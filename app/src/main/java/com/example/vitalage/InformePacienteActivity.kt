    package com.example.vitalage

    import android.R
    import android.content.Context
    import android.content.Intent
    import android.graphics.Bitmap
    import android.graphics.Color
    import android.graphics.Paint
    import android.graphics.pdf.PdfDocument
    import android.os.Bundle
    import android.os.Environment
    import android.util.Log
    import android.view.View
    import android.widget.ArrayAdapter
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.example.vitalage.clases.PromediosSignosVitales
    import com.example.vitalage.databinding.ActivityInformePacienteBinding
    import com.github.mikephil.charting.charts.BarChart
    import com.github.mikephil.charting.charts.LineChart
    import com.github.mikephil.charting.components.Legend
    import com.github.mikephil.charting.components.LegendEntry
    import com.github.mikephil.charting.components.XAxis
    import com.github.mikephil.charting.data.BarData
    import com.github.mikephil.charting.data.BarDataSet
    import com.github.mikephil.charting.data.BarEntry
    import com.github.mikephil.charting.data.Entry
    import com.github.mikephil.charting.data.LineData
    import com.github.mikephil.charting.data.LineDataSet
    import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
    import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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

            findViewById<LinearLayout>(com.example.vitalage.R.id.btnHomeContainer).setOnClickListener {
                startActivity(Intent(this, PatientListActivity::class.java))
            }

            findViewById<LinearLayout>(com.example.vitalage.R.id.btnProfileContainer).setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
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
                calcularPromediosSignosVitales(patientId, periodoSeleccionado) { promedios ->
                    if (promedios != null) {
                        // Mostrar gráfico
                        mostrarGraficoSignosVitales(binding.barChartSignosVitales, promedios)
                        obtenerYGraficarEscalas(patientId,
                            periodoSeleccionado,
                            binding.lineChartEscalas,
                            this
                        )
                    } else {
                        Toast.makeText(this, "No hay datos para graficar", Toast.LENGTH_SHORT).show()
                    }
                }

            }

            binding.btnDescargarPdf.setOnClickListener {
                binding.barChartSignosVitales.post {
                    val bitmapSignos = binding.barChartSignosVitales.chartBitmap

                    binding.lineChartEscalas.post {
                        val bitmapEscalas = binding.lineChartEscalas.chartBitmap

                        generarPdfDesdeTexto(
                            texto = binding.tvVistaInforme.text.toString(),
                            bitmap1 = bitmapSignos,
                            bitmap2 = bitmapEscalas
                        )
                    }
                }
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
                        builder.appendLine("Fecha: ${it["fecha"]}")
                        builder.appendLine("   FC: ${it["frecuencia_cardiaca"]} bpm   FR: ${it["frecuencia_respiratoria"]} rpm")
                        builder.appendLine("   Oxígeno: ${it["saturacion_oxigeno"]}%   Temp: ${it["temperatura"]} °C")
                        builder.appendLine("   PA: ${it["presion_arterial"]}   Peso: ${it["peso"]} kg   IMC: ${it["imc"]}")
                        builder.appendLine("   Encargado: ${it["encargado"]}\n")
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
                        builder.appendLine("$fechaFormateada")
                        builder.appendLine("   Medicamento: ${it["medicamento"]}")
                        builder.appendLine("   Dosis: ${it["dosis"]} mg   Cantidad: ${it["cantidad"]}u")
                        builder.appendLine("   Administrado por: ${it["usuario"]}\n")
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
                        val realizada = it["realizada"] as? Boolean ?: false
                        builder.appendLine("Fecha: ${it["fecha"]}")
                        builder.appendLine("   Tipo: ${it["tipo"]}   Realizada: ${if (realizada) "Si" else "No"}")
                        builder.appendLine("   Encargado: ${it["encargado"]}\n")
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
                        builder.appendLine("Fecha: ${it["fecha"]}")
                        builder.appendLine("   Tipo: ${it["tipo"]}   Puntaje: ${it["puntaje"]}")
                        builder.appendLine("   Encargado: ${it["encargado"]}\n")
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
                        builder.appendLine("Fecha: ${it["fecha"]}")
                        builder.appendLine("   Título: ${it["titulo"]}")
                        builder.appendLine("   Descripción: ${it["descripcion"]}")
                        builder.appendLine("   Enfermera: ${it["enfermera"]}\n")
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



        private fun generarPdfDesdeTexto(texto: String, bitmap1: Bitmap? = null, bitmap2: Bitmap? = null) {
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

            // Imprimir el texto línea por línea
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

// Incluir gráfico 1 escalado
            bitmap1?.let {
                val scaledBitmap = escalarBitmap(it, (pageWidth - 2 * margin).toInt())

                if (y + scaledBitmap.height > pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin
                }

                canvas.drawBitmap(scaledBitmap, margin, y, null)
                y += scaledBitmap.height + 20f
            }

// Incluir gráfico 2 escalado
            bitmap2?.let {
                val scaledBitmap = escalarBitmap(it, (pageWidth - 2 * margin).toInt())

                if (y + scaledBitmap.height > pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin
                }

                canvas.drawBitmap(scaledBitmap, margin, y, null)
                y += scaledBitmap.height + 20f
            }

            // Finalizar última página
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

        fun calcularPromediosSignosVitales(
            pacienteId: String,
            periodo: String,
            onResultado: (PromediosSignosVitales?) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("Pacientes").document(pacienteId)

            docRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.w("Firestore", "No se encontró el paciente con ID: $pacienteId")
                    onResultado(null)
                    return@addOnSuccessListener
                }

                val signos = document.get("signos_vitales") as? List<Map<String, Any>> ?: emptyList()
                if (signos.isEmpty()) {
                    Log.w("Firestore", "El paciente no tiene signos vitales registrados.")
                    onResultado(null)
                    return@addOnSuccessListener
                }

                val hoy = LocalDate.now()
                val desde = when (periodo.lowercase(Locale.ROOT)) {
                    "mensual" -> hoy.minusMonths(1)
                    "semestral" -> hoy.minusMonths(6)
                    else -> hoy.minusMonths(1)
                }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                var totalFc = 0f
                var totalFr = 0f
                var totalOxigeno = 0f
                var totalTemperatura = 0f
                var totalPeso = 0f
                var totalImc = 0f
                var count = 0

                for (registro in signos) {
                    val fechaStr = registro["fecha"] as? String
                    val fechaRegistro = try {
                        fechaStr?.let {
                            val soloFecha = it.split(" ")[0] // "2025-03-27 21:02" → "2025-03-27"
                            LocalDate.parse(soloFecha, formatter)
                        }
                    } catch (e: Exception) {
                        null
                    }

                    if (fechaRegistro != null && fechaRegistro !in desde..hoy) continue

                    val fc = (registro["frecuencia_cardiaca"] as? Number)?.toFloat()
                    val fr = (registro["frecuencia_respiratoria"] as? Number)?.toFloat()
                    val oxigeno = (registro["saturacion_oxigeno"] as? Number)?.toFloat()
                    val temperatura = (registro["temperatura"] as? Number)?.toFloat()
                    val peso = (registro["peso"] as? Number)?.toFloat()
                    val imc = (registro["imc"] as? Number)?.toFloat()

                    if (fc != null && fr != null && oxigeno != null && temperatura != null && peso != null && imc != null) {
                        totalFc += fc
                        totalFr += fr
                        totalOxigeno += oxigeno
                        totalTemperatura += temperatura
                        totalPeso += peso
                        totalImc += imc
                        count++
                    } else {
                        Log.w("Signos", "Registro inválido o incompleto: $registro")
                    }
                }

                if (count == 0) {
                    Log.w("Promedios", "No se encontraron registros válidos en el rango $periodo")
                    onResultado(null)
                } else {
                    val promedios = PromediosSignosVitales(
                        fc = totalFc / count,
                        fr = totalFr / count,
                        oxigeno = totalOxigeno / count,
                        temperatura = totalTemperatura / count,
                        peso = totalPeso / count,
                        imc = totalImc / count
                    )
                    onResultado(promedios)
                }

            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error obteniendo el documento: ${e.message}")
                onResultado(null)
            }
        }

        fun mostrarGraficoSignosVitales(barChart: BarChart, datos: PromediosSignosVitales) {
            barChart.visibility = View.VISIBLE
            binding.tituloSignos.visibility = View.VISIBLE

            // 1. Entradas del gráfico de barras
            val entries = listOf(
                BarEntry(0f, datos.fc),
                BarEntry(1f, datos.fr),
                BarEntry(2f, datos.oxigeno),
                BarEntry(3f, datos.temperatura),
                BarEntry(4f, datos.peso),
                BarEntry(5f, datos.imc)
            )

            // 2. Etiquetas para el eje X (puedes cambiarlas por nombres completos si deseas)
            val labels = listOf("FC", "FR", "O₂", "Temp", "Peso", "IMC")

            // 3. Dataset con los valores y colores personalizados
            val dataSet = BarDataSet(entries, "").apply {
                valueTextSize = 12f
                colors = listOf(
                    Color.parseColor("#4CAF50"), // Verde
                    Color.parseColor("#2196F3"), // Azul
                    Color.parseColor("#FFC107"), // Amarillo
                    Color.parseColor("#F44336"), // Rojo
                    Color.parseColor("#9C27B0"), // Morado
                    Color.parseColor("#00BCD4")  // Cyan
                )
            }

            // 4. Asignar los datos al gráfico
            barChart.data = BarData(dataSet)

            // 5. Configurar eje X
            barChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 12f
            }

            // 6. Configurar eje Y
            barChart.axisRight.isEnabled = false
            barChart.axisLeft.axisMinimum = 0f

            // 7. Descripción desactivada
            barChart.description.isEnabled = false

            // 8. Desactivar zoom
            barChart.setScaleEnabled(false)
            barChart.setPinchZoom(false)
            barChart.isDoubleTapToZoomEnabled = false


            barChart.legend.isWordWrapEnabled = true

            // 10. Animación y render final
            barChart.setFitBars(true)
            barChart.animateY(1000)
            barChart.invalidate()
        }

        fun obtenerYGraficarEscalas(
            pacienteId: String,
            periodo: String,
            lineChart: LineChart,
            contexto: Context // Para logs si deseas usar Toasts o errores
        ) {
            lineChart.visibility = View.VISIBLE
            binding.tituloEscalas.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("Pacientes").document(pacienteId)

            val hoy = LocalDate.now()
            val desde = when (periodo) {
                "Mensual" -> hoy.minusMonths(1)
                "Semestral" -> hoy.minusMonths(6)
                else -> hoy.minusMonths(1)
            }

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            docRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.w("Firestore", "Paciente no encontrado: $pacienteId")
                    return@addOnSuccessListener
                }

                val escalas = document.get("escalas") as? List<Map<String, Any>> ?: emptyList()

                val escalasFiltradas = escalas.filter {
                    val fechaStr = it["fecha"] as? String
                    try {
                        fechaStr?.let { f ->
                            val fechaEscala = LocalDate.parse(f, formatter)
                            fechaEscala in desde..hoy
                        } ?: false
                    } catch (e: Exception) {
                        Log.e("Escalas", "Error al parsear fecha: $fechaStr")
                        false
                    }
                }

                mostrarEvolucionEscalas(lineChart, escalasFiltradas)

            }.addOnFailureListener {
                Log.e("Firestore", "Error al obtener paciente: ${it.message}")
            }
        }

        fun mostrarEvolucionEscalas(lineChart: LineChart, escalas: List<Map<String, Any>>) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val tipoMap = mutableMapOf<String, MutableList<Entry>>() // tipo -> datos
            val fechasOrdenadas = mutableListOf<String>() // para el eje X

            // Mapeamos fechas únicas ordenadas para el eje X
            val fechasUnicas = escalas.mapNotNull {
                it["fecha"] as? String
            }.distinct().sortedBy {
                try {
                    LocalDate.parse(it, formatter)
                } catch (e: Exception) {
                    null
                }
            }

            // Mapa fecha → índice
            val fechaIndexMap = fechasUnicas.mapIndexed { index, fecha -> fecha to index.toFloat() }.toMap()

            // Recolectar datos por tipo de escala
            for (escala in escalas) {
                val tipo = escala["tipo"] as? String ?: continue
                val puntaje = (escala["puntaje"] as? Number)?.toFloat() ?: continue
                val fecha = escala["fecha"] as? String ?: continue
                val index = fechaIndexMap[fecha] ?: continue

                tipoMap.getOrPut(tipo) { mutableListOf() }.add(Entry(index, puntaje))
            }

            // Etiquetas para el eje X (solo día)
            val etiquetasX = fechasUnicas.map {
                try {
                    val date = LocalDate.parse(it, formatter)
                    date.dayOfMonth.toString().padStart(2, '0') // ejemplo: "01", "02"
                } catch (e: Exception) {
                    "??"
                }
            }

            // Dataset y colores
            val dataSets = mutableListOf<ILineDataSet>()
            val colores = listOf("#4CAF50", "#2196F3", "#FFC107", "#F44336", "#9C27B0", "#00BCD4")
            var colorIndex = 0

            for ((tipo, entries) in tipoMap) {
                val dataSet = LineDataSet(entries, tipo).apply {
                    color = Color.parseColor(colores[colorIndex % colores.size])
                    lineWidth = 2f
                    valueTextSize = 10f
                    setDrawCircles(true)
                    setDrawValues(true)
                    setCircleColor(color)
                    mode = LineDataSet.Mode.LINEAR
                }
                dataSets.add(dataSet)
                colorIndex++
            }

            // Configuración del gráfico
            lineChart.data = LineData(dataSets)

            lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 10f
                valueFormatter = IndexAxisValueFormatter(etiquetasX)
            }

            lineChart.axisRight.isEnabled = false
            lineChart.axisLeft.axisMinimum = 0f
            lineChart.description.isEnabled = false
            lineChart.legend.isEnabled = true
            lineChart.setScaleEnabled(false)
            lineChart.setPinchZoom(false)
            lineChart.animateX(1000)
            lineChart.invalidate()
        }

        fun escalarBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
            val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt()
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }

















    }
