    package com.example.vitalage

    import android.R
    import android.graphics.Color
    import android.graphics.Paint
    import android.graphics.pdf.PdfDocument
    import android.os.Bundle
    import android.os.Environment
    import android.util.Log
    import android.widget.ArrayAdapter
    import android.widget.ImageView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.example.vitalage.databinding.ActivityInformePacienteBinding
    import com.google.firebase.Timestamp
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener
    import com.google.firebase.firestore.FirebaseFirestore
    import java.io.File
    import java.io.FileOutputStream
    import java.text.SimpleDateFormat
    import java.time.LocalDate
    import java.time.ZoneId
    import java.time.format.DateTimeFormatter
    import java.util.Locale

    class InformePacienteActivity : AppCompatActivity() {

        private lateinit var binding: ActivityInformePacienteBinding
        private lateinit var patientId: String
        private lateinit var patientName: String
        private lateinit var firestore: FirebaseFirestore
        private var usuarioActual: String = "Desconocido"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityInformePacienteBinding.inflate(layoutInflater)
            setContentView(binding.root)

            firestore = FirebaseFirestore.getInstance()

            // 🔙 Botón de volver
            binding.btnBack.setOnClickListener {
                finish()
            }

            obtenerNombreUsuario { nombre ->
                usuarioActual = nombre
                binding.tvUser.text = "Usuario: $usuarioActual"
            }

            findViewById<ImageView>(com.example.vitalage.R.id.btnBack).setOnClickListener {
                finish()
            }


            // Recibe datos
            patientId = intent.getStringExtra("patient_id") ?: ""
            patientName = intent.getStringExtra("patient_name") ?: "Paciente"

            binding.tvNombrePaciente.text = "Paciente: $patientName"

            // Spinner de periodo
            val periodos = listOf("Mensual", "Semestral")
            val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, periodos)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spPeriodo.adapter = adapter

            binding.btnGenerarInforme.setOnClickListener {
                val periodoSeleccionado = binding.spPeriodo.selectedItem.toString()
                generarInforme(periodoSeleccionado)
            }

            binding.btnDescargarPdf.setOnClickListener {
                generarPdfDesdeTexto(binding.tvVistaInforme.text.toString())
            }
        }


        private fun generarInforme(periodo: String) {
            val db = FirebaseFirestore.getInstance()
            val pacienteRef = db.collection("Pacientes").document(patientId)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatterNotas = DateTimeFormatter.ofPattern("d/M/yyyy")
            val hoy = LocalDate.now()
            val hasta = hoy
            val desde = when (periodo) {
                "Mensual" -> hoy.minusMonths(1)
                "Semestral" -> hoy.minusMonths(6)
                else -> hoy.minusMonths(1)
            }

            pacienteRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val builder = StringBuilder()

                    builder.appendLine("╔══════════════════════════════════════════╗")
                    builder.appendLine("              INFORME CLÍNICO              ")
                    builder.appendLine("╚══════════════════════════════════════════╝\n")
                    builder.appendLine("👤 Nombre del paciente: $patientName")
                    builder.appendLine("📆 Periodo del informe: $periodo (${desde.format(formatter)} a ${hoy.format(formatter)})")
                    builder.appendLine("🕒 Fecha de generación: ${hoy.format(formatter)}\n")

                    // 1. SIGNOS VITALES
                    builder.appendLine("📍 1. SIGNOS VITALES")
                    builder.appendLine("──────────────────────────────────────────")
                    val signos = document.get("signos_vitales") as? List<Map<String, Any>> ?: emptyList()
                    if (signos.isEmpty()) builder.appendLine("❌ No se encontraron registros.")
                    signos.filter {
                        val fechaStr = it["fecha"] as? String
                        try {
                            fechaStr?.let { f ->
                                val soloFecha = f.split(" ")[0].replace("/", "-")
                                val fecha = LocalDate.parse(soloFecha, formatter)
                                fecha in desde..hasta
                            } ?: false
                        } catch (e: Exception) {
                            false
                        }
                    }.forEach {
                        builder.appendLine("📅 Fecha: ${it["fecha"]}")
                        builder.appendLine("   ❤️ FC: ${it["frecuencia_cardiaca"]} bpm   💨 FR: ${it["frecuencia_respiratoria"]} rpm")
                        builder.appendLine("   🫁 Oxígeno: ${it["saturacion_oxigeno"]}%   🌡️ Temp: ${it["temperatura"]} °C")
                        builder.appendLine("   🩸 PA: ${it["presion_arterial"]}   ⚖️ Peso: ${it["peso"]} kg   📊 IMC: ${it["imc"]}")
                        builder.appendLine("   👩 Encargado: ${it["encargado"]}\n")
                    }

                    // 2. MEDICAMENTOS ADMINISTRADOS
                    builder.appendLine("💊 2. MEDICAMENTOS ADMINISTRADOS")
                    builder.appendLine("──────────────────────────────────────────")
                    val dosis = document.get("historial_dosis") as? List<Map<String, Any>> ?: emptyList()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    if (dosis.isEmpty()) builder.appendLine("❌ No se encontraron registros.")
                    dosis.filter {
                        val fecha = it["fecha_hora"] as? Timestamp
                        fecha?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()?.isAfter(desde) ?: false
                    }.forEach {
                        val fechaFormateada = (it["fecha_hora"] as? Timestamp)?.toDate()?.let { date -> sdf.format(date) }
                        builder.appendLine("📅 $fechaFormateada")
                        builder.appendLine("   💊 Medicamento: ${it["medicamento"]}")
                        builder.appendLine("   🧪 Dosis: ${it["dosis"]} mg   Cantidad: ${it["cantidad"]}u")
                        builder.appendLine("   👩 Administrado por: ${it["usuario"]}\n")
                    }

                    // 3. TERAPIAS
                    builder.appendLine("📋 3. TERAPIAS")
                    builder.appendLine("──────────────────────────────────────────")
                    val terapias = document.get("terapias") as? List<Map<String, Any>> ?: emptyList()
                    if (terapias.isEmpty()) builder.appendLine("❌ No se encontraron registros.")
                    terapias.filter {
                        val fecha = it["fecha"] as? String
                        try {
                            fecha?.let { f -> LocalDate.parse(f, formatterNotas) in desde..hasta } ?: false
                        } catch (e: Exception) {
                            false
                        }
                    }.forEach {
                        builder.appendLine("📅 Fecha: ${it["fecha"]}")
                        builder.appendLine("   🧠 Tipo: ${it["tipo"]}   ✅ Realizada: ${it["realizada"]}")
                        builder.appendLine("   👨 Encargado: ${it["encargado"]}\n")
                    }

                    // 4. ESCALAS APLICADAS
                    builder.appendLine("🧠 4. ESCALAS APLICADAS")
                    builder.appendLine("──────────────────────────────────────────")
                    val escalas = document.get("escalas") as? List<Map<String, Any>> ?: emptyList()

                    if (escalas.isEmpty()) builder.appendLine("❌ No se encontraron registros.")

                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                    escalas.filter {
                        val fecha = it["fecha"] as? String
                        try {
                            fecha?.let { f -> LocalDate.parse(f, formatter).isAfter(desde) } ?: false
                        } catch (e: Exception) {
                            false
                        }
                    }.forEach {
                        builder.appendLine("📅 Fecha: ${it["fecha"]}")
                        builder.appendLine("   📌 Tipo: ${it["tipo"]}   🧮 Puntaje: ${it["puntaje"]}")
                        builder.appendLine("   👩 Encargado: ${it["encargado"]}\n")
                    }


                    // 5. NOTAS DE ENFERMERÍA
                    builder.appendLine("📝 5. NOTAS DE ENFERMERÍA")
                    builder.appendLine("──────────────────────────────────────────")
                    val notas = document.get("notasEnfermeria") as? List<Map<String, Any>> ?: emptyList()
                    if (notas.isEmpty()) builder.appendLine("❌ No se encontraron registros.")

    // Define el formato adecuado para las fechas (yyyy-MM-dd HH:mm)
                    val formatterNotas = DateTimeFormatter.ofPattern("yyyy-MM-dd")  // Cambié el formato para solo fecha

                    notas.filter {
                        val fecha = it["fecha"] as? String
                        try {
                            fecha?.let { f ->
                                // Separamos la fecha y hora
                                val soloFecha = f.split(" ")[0] // Obtenemos solo la fecha
                                val fechaNota = LocalDate.parse(soloFecha, formatterNotas)  // Parseamos la fecha (solo la parte de la fecha)

                                // Depuración: imprime la fecha de la nota para ver si es válida
                                Log.d("FechaNota", "Fecha nota: $fechaNota, Rango: ${desde} - ${hasta}")

                                // Filtramos si la fecha está en el rango desde..hasta
                                fechaNota in desde..hasta
                            } ?: false
                        } catch (e: Exception) {
                            Log.e("ErrorFechaNota", "Error al procesar la fecha de la nota: ${e.message}")
                            false  // Si no se puede parsear la fecha, la excluimos
                        }
                    }.forEach {
                        builder.appendLine("📅 Fecha: ${it["fecha"]}")
                        builder.appendLine("   📝 Título: ${it["titulo"]}")
                        builder.appendLine("   📄 Descripción: ${it["descripcion"]}")
                        builder.appendLine("   👩 Enfermera: ${it["enfermera"]}\n")
                    }

    // Mostrar el informe generado
                    binding.tvVistaInforme.text = builder.toString()




                    binding.tvVistaInforme.text = builder.toString()

                } else {
                    binding.tvVistaInforme.text = "❌ No se encontraron datos para este paciente."
                }
            }.addOnFailureListener {
                Toast.makeText(this, "⚠️ Error al generar informe: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }





        private fun generarPdfDesdeTexto(texto: String) {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            val lineHeight = paint.descent() - paint.ascent() + 4f
            val lines = texto.split("\n")

            paint.textSize = 12f
            paint.color = Color.BLACK

            var pageNumber = 1
            var y = margin - paint.ascent()

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            for (line in lines) {
                if (y + lineHeight > pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin - paint.ascent()
                }
                canvas.drawText(line, margin, y, paint)
                y += lineHeight
            }

            pdfDocument.finishPage(page)

            try {
                val fileName = "informe_${patientName.replace(" ", "_")}.pdf"
                val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(this, "PDF generado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        }

        private fun obtenerNombreUsuario(callback: (String) -> Unit) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid == null) {
                Log.e("Firebase", "No se encontró un usuario autenticado.")
                callback("Desconocido")
                return
            }

            Log.d("Firebase", "UID del usuario autenticado: $uid")

            // 🔥 Corregimos la referencia según la estructura: user -> users -> {UID}
            val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Intentamos obtener el nombre desde ambas posibles claves
                        val nombreUsuario = snapshot.child("nombre").value as? String
                            ?: snapshot.child("nombre_usuario").value as? String
                            ?: "Desconocido"

                        Log.d("Firebase", "Nombre obtenido de la base de datos: $nombreUsuario")
                        callback(nombreUsuario)
                    } else {
                        Log.e("Firebase", "No se encontró el usuario en la base de datos.")
                        callback("Desconocido")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al obtener el nombre: ${error.message}")
                    callback("Desconocido")
                }
            })
        }





    }
