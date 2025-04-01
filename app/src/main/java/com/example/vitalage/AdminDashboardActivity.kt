package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.HashMap

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val tvResidentesActivos = findViewById<TextView>(R.id.tvResidentesActivos)
        val tvPersonalSalud = findViewById<TextView>(R.id.tvPersonalSalud)
        val layoutNotas = findViewById<LinearLayout>(R.id.layout_alert_notes)
        val layoutMeds = findViewById<LinearLayout>(R.id.layout_alert_meds)
        val barChart = findViewById<BarChart>(R.id.barChartMeds)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            Toast.makeText(this, "Perfil en construcción", Toast.LENGTH_SHORT).show()
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                tvResidentesActivos.text = result.size().toString()
            }
            .addOnFailureListener {
                Log.e("Dashboard", "Error pacientes: \${it.message}")
                tvResidentesActivos.text = "0"
            }

        val dbRealtime = FirebaseDatabase.getInstance().getReference("user/users")
        dbRealtime.get().addOnSuccessListener { snapshot ->
            var count = 0
            for (userSnap in snapshot.children) {
                val rol = userSnap.child("rol").value as? String ?: ""
                if (rol.equals("enfermera", true) || rol.equals("médico", true)) count++
            }
            tvPersonalSalud.text = count.toString()
        }.addOnFailureListener {
            Log.e("Dashboard", "Error personal salud: \${it.message}")
            tvPersonalSalud.text = "0"
        }

        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                layoutNotas.removeAllViews()
                for (doc in result) {
                    val notas = doc.get("notasEnfermeria") as? List<Map<String, Any>> ?: continue
                    for (nota in notas) {
                        val fecha = nota["fecha"] as? String ?: continue
                        val enfermera = nota["enfermera"] as? String ?: ""
                        val titulo = nota["titulo"] as? String ?: ""
                        val descripcion = nota["descripcion"] as? String ?: ""

                        val card = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(16, 16, 16, 16)
                            setBackgroundResource(android.R.color.white)
                            elevation = 4f
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(0, 0, 0, 16) }
                        }

                        val icon = ImageView(this).apply {
                            setImageResource(R.drawable.ic_note)
                            setColorFilter(getColor(R.color.md_blue_500))
                            layoutParams = LinearLayout.LayoutParams(48, 48)
                        }

                        val tvTitulo = TextView(this).apply {
                            text = titulo.ifBlank { "Sin título" }
                            textSize = 16f
                            setTextColor(getColor(R.color.black))
                            setPadding(0, 8, 0, 4)
                        }

                        val tvDetalle = TextView(this).apply {
                            text = "$fecha - $enfermera: \${descripcion.take(100)}"
                            textSize = 14f
                            setTextColor(getColor(R.color.black))
                        }

                        card.addView(icon)
                        card.addView(tvTitulo)
                        card.addView(tvDetalle)
                        layoutNotas.addView(card)
                    }
                }
            }

        layoutMeds.removeAllViews()

        val hoy = Calendar.getInstance().time
        val limite = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time
        val formatosFecha = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("MM/yyyy", Locale.getDefault())
        )

        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                var hayMedicamentos = false

                for (doc in result) {
                    val nombrePaciente = doc.getString("nombre") ?: "Paciente sin nombre"
                    val meds = doc.get("medicamentos") as? List<Map<String, Any>> ?: continue

                    for (med in meds) {
                        val fechaStr = med["fecha_vencimiento"] as? String ?: continue
                        val medicamentoId = med["medicamento_id"] as? String ?: continue

                        var fechaParseada: java.util.Date? = null
                        for (formato in formatosFecha) {
                            try {
                                fechaParseada = formato.parse(fechaStr)
                                if (fechaParseada != null) break
                            } catch (_: Exception) {}
                        }

                        if (fechaParseada != null) {
                            val calFecha = Calendar.getInstance().apply { time = fechaParseada }
                            val calHoy = Calendar.getInstance()
                            val calLimite = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }

                            if (!calFecha.before(calHoy) && !calFecha.after(calLimite)) {
                                db.collection("Medicamentos").document(medicamentoId).get()
                                    .addOnSuccessListener { medDoc ->
                                        val nombreMed = medDoc.getString("nombre") ?: "Medicamento sin nombre"
                                        val view = TextView(this).apply {
                                            text = "$nombrePaciente - $nombreMed vence: $fechaStr"
                                            textSize = 14f
                                            setTextColor(getColor(R.color.black))
                                            setPadding(0, 0, 0, 8)
                                        }
                                        layoutMeds.addView(view)
                                    }
                                hayMedicamentos = true
                            }
                        } else {
                            Log.e("Dashboard", "Error parsing fecha: $fechaStr")
                        }
                    }
                }

                if (!hayMedicamentos) {
                    val sinAlertas = TextView(this).apply {
                        text = "No hay medicamentos por vencer en los próximos 7 días."
                        textSize = 14f
                        setTextColor(getColor(R.color.black))
                    }
                    layoutMeds.addView(sinAlertas)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar medicamentos por vencer", Toast.LENGTH_SHORT).show()
                Log.e("Dashboard", "Error medicamentos: \${it.message}")
            }

        // 🔹 Gráfico de dosis administradas por día
        val formatoFecha = SimpleDateFormat("dd/MM", Locale.getDefault())
        val dias = mutableListOf<String>()
        val dosisPorDia = HashMap<String, Int>()

        val cal = Calendar.getInstance()
        for (i in 6 downTo 0) {
            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val fecha = formatoFecha.format(cal.time)
            dias.add(fecha)
            dosisPorDia[fecha] = 0
        }

        db.collection("Pacientes").get().addOnSuccessListener { result ->
            for (doc in result) {
                val historial = doc.get("historial_dosis") as? List<Map<String, Any>> ?: continue
                for (registro in historial) {
                    val timestamp = registro["fecha_hora"]
                    if (timestamp is com.google.firebase.Timestamp) {
                        val fechaStr = formatoFecha.format(timestamp.toDate())
                        dosisPorDia[fechaStr] = dosisPorDia[fechaStr]?.plus(1) ?: 1
                    }
                }
            }

            val entries = dosisPorDia.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }
            val dataSet = BarDataSet(entries, "Dosis Administradas")
            val barData = BarData(dataSet)

            barChart.data = barData
            barChart.description.isEnabled = false
            barChart.axisRight.isEnabled = false
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(dias)
            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChart.xAxis.granularity = 1f
            barChart.invalidate()
        }
    }
}