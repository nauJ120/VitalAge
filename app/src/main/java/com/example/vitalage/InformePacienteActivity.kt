package com.example.vitalage

import android.R
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitalage.databinding.ActivityInformePacienteBinding
import com.google.firebase.Timestamp
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformePacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

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
                builder.append("📋 Informe de salud de $patientName\n")
                builder.append("Periodo: $periodo (${desde.format(formatter)} a ${hoy.format(formatter)})\n\n")

                // 1. Signos Vitales
                builder.append("🩺 Signos vitales:\n")
                val signos = document.get("signos_vitales") as? List<Map<String, Any>> ?: emptyList()
                signos.filter {
                    val fecha = it["fecha"] as? String
                    try {
                        fecha?.let { f -> LocalDate.parse(f, formatter).isAfter(desde) } ?: false
                    } catch (e: Exception) {
                        false
                    }
                }.forEach {
                    builder.append("- ${it["fecha"]}: Temp ${it["temperatura"]}°C, FC ${it["frecuencia_cardiaca"]}, PA ${it["presion_arterial"]}\n")
                }

                // 2. Dosis Administradas
                builder.append("\n💊 Dosis administradas:\n")
                val dosis = document.get("historial_dosis") as? List<Map<String, Any>> ?: emptyList()

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) // Formato legible

                dosis.filter {
                    val fecha = it["fecha_hora"] as? Timestamp
                    fecha?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()?.isAfter(desde) ?: false
                }.forEach {
                    val timestamp = it["fecha_hora"] as? Timestamp
                    val fechaFormateada = timestamp?.toDate()?.let { date -> sdf.format(date) } ?: "Fecha desconocida"
                    val medicamento = it["medicamento"] ?: "Desconocido"
                    val cantidad = it["cantidad"] ?: "-"
                    val dosisMg = it["dosis"] ?: "-"
                    val usuario = it["usuario"] ?: "Sin registrar"

                    builder.append("- $fechaFormateada: $medicamento, ${cantidad}u, ${dosisMg}mg ($usuario)\n")
                }


                // 3. Terapias
                builder.append("\n📋 Terapias:\n")
                val terapias = document.get("terapias") as? List<Map<String, Any>> ?: emptyList()
                terapias.filter {
                    val fecha = it["fecha"] as? String
                    try {
                        fecha?.let { f ->
                            val fechaLocal = LocalDate.parse(f, formatterNotas)
                            fechaLocal in desde..hasta
                        } ?: false
                    } catch (e: Exception) {
                        false
                    }
                }.forEach {
                    builder.append("- ${it["fecha"]}: ${it["tipo"]} - Realizada: ${it["realizada"]}\n")
                }

                // 4. Escalas
                builder.append("\n🧠 Escalas aplicadas:\n")
                val escalas = document.get("escalas") as? List<Map<String, Any>> ?: emptyList()
                escalas.filter {
                    val fecha = it["fecha"] as? String
                    try {
                        fecha?.let { f -> LocalDate.parse(f, formatter).isAfter(desde) } ?: false
                    } catch (e: Exception) {
                        false
                    }
                }.forEach {
                    builder.append("- ${it["fecha"]}: ${it["tipo"]} - Puntaje: ${it["puntaje"]}\n")
                }

                // 5. Notas de enfermería
                builder.append("\n📝 Notas de enfermería:\n")
                val notas = document.get("notasEnfermeria") as? List<Map<String, Any>> ?: emptyList()
                notas.filter {
                    val fecha = it["fecha"] as? String
                    try {
                        fecha?.let { f ->
                            val soloFecha = f.split(" ")[0]
                            val fechaNota = LocalDate.parse(soloFecha, formatter)
                            fechaNota in desde..hasta
                        } ?: false
                    } catch (e: Exception) {
                        false
                    }
                }.forEach {
                    builder.append("- ${it["fecha"]}: ${it["titulo"]} - ${it["descripcion"]}\n")
                }

                binding.tvVistaInforme.text = builder.toString()
            } else {
                binding.tvVistaInforme.text = "No se encontraron datos para este paciente."
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al generar informe: ${it.message}", Toast.LENGTH_SHORT).show()
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


}
