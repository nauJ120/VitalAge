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
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val tvResidentesActivos = findViewById<TextView>(R.id.tvResidentesActivos)
        val tvPersonalSalud = findViewById<TextView>(R.id.tvPersonalSalud)
        val layoutNotas = findViewById<LinearLayout>(R.id.layout_alert_notes)
        val layoutMeds = findViewById<LinearLayout>(R.id.layout_alert_meds)
        val barChart = findViewById<BarChart>(R.id.barChartMeds)

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            findViewById<TextView>(R.id.tvUser).text = "Administrador: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
            finish()
        }
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            Toast.makeText(this, "Perfil en construcción", Toast.LENGTH_SHORT).show()
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("Pacientes").get().addOnSuccessListener { result ->
            tvResidentesActivos.text = result.size().toString()
        }.addOnFailureListener {
            Log.e("Dashboard", "Error pacientes: ${it.message}")
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
            Log.e("Dashboard", "Error personal salud: ${it.message}")
            tvPersonalSalud.text = "0"
        }

        db.collection("Pacientes").get().addOnSuccessListener { result ->
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
                        text = "$fecha - $enfermera: $descripcion"
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

        db.collection("Pacientes").get().addOnSuccessListener { result ->
            var hayMedicamentos = false
            for (doc in result) {
                val nombrePaciente = doc.getString("nombre") ?: "Paciente sin nombre"
                val meds = doc.get("medicamentos") as? List<Map<String, Any>> ?: continue
                for (med in meds) {
                    val fechaStr = med["fecha_vencimiento"] as? String ?: continue
                    val medicamentoId = med["medicamento_id"] as? String ?: continue

                    var fechaParseada: Date? = null
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
                            db.collection("Medicamentos").document(medicamentoId).get().addOnSuccessListener { medDoc ->
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
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar medicamentos por vencer", Toast.LENGTH_SHORT).show()
            Log.e("Dashboard", "Error medicamentos: ${it.message}")
        }

        // 🔹 Gráfico actualizado: mostrar solo últimas 4 fechas de administración
        val formatoFecha = SimpleDateFormat("dd/MM", Locale.getDefault())
        val dosisPorMedicamentoPorFecha = mutableMapOf<String, MutableMap<String, Int>>()
        val fechasLabels = mutableSetOf<String>()

        db.collection("Pacientes").get().addOnSuccessListener { result ->
            for (doc in result) {
                val historial = doc.get("historial_dosis") as? List<Map<String, Any>> ?: continue
                for (registro in historial) {
                    val timestamp = registro["fecha_hora"] as? com.google.firebase.Timestamp ?: continue
                    val medicamento = registro["medicamento"] as? String ?: continue
                    val fecha = formatoFecha.format(timestamp.toDate())
                    fechasLabels.add(fecha)
                    val mapa = dosisPorMedicamentoPorFecha.getOrPut(medicamento) { mutableMapOf() }
                    mapa[fecha] = mapa.getOrDefault(fecha, 0) + 1
                }
            }

            val ultimasFechas = fechasLabels
                .sortedByDescending { formatoFecha.parse(it) }
                .take(4)
                .sortedBy { formatoFecha.parse(it) }

            val medicamentos = dosisPorMedicamentoPorFecha.keys.toList().sorted()
            val colors = listOf(
                R.color.md_blue_500, R.color.md_green_500, R.color.md_red_500,
                R.color.md_orange_500, R.color.md_purple_500, R.color.md_teal_500
            )

            val dataSets = mutableListOf<BarDataSet>()

            medicamentos.forEachIndexed { index, medicamento ->
                val entries = ultimasFechas.mapIndexed { i, fecha ->
                    val cantidad = dosisPorMedicamentoPorFecha[medicamento]?.get(fecha) ?: 0
                    BarEntry(i.toFloat(), cantidad.toFloat())
                }
                val dataSet = BarDataSet(entries, medicamento)
                dataSet.color = getColor(colors[index % colors.size])
                dataSet.valueTextSize = 12f
                dataSet.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = value.toInt().toString()
                }
                dataSets.add(dataSet)
            }

            val barData = BarData(*dataSets.toTypedArray())
            barData.barWidth = 0.15f
            barChart.data = barData
            barChart.description.isEnabled = false
            barChart.axisRight.isEnabled = false
            barChart.axisLeft.granularity = 1f
            barChart.axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
            barChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(ultimasFechas)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setCenterAxisLabels(true)
                axisMinimum = 0f
                axisMaximum = ultimasFechas.size.toFloat()
            }
            if (dataSets.size > 1) {
                barChart.groupBars(0f, 0.3f, 0.05f)
            }
            barChart.invalidate()
        }
    }

    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return callback("Desconocido")
        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombreUsuario = snapshot.child("nombre").value as? String
                    ?: snapshot.child("nombre_usuario").value as? String
                    ?: "Desconocido"
                callback(nombreUsuario)
            }
            override fun onCancelled(error: DatabaseError) {
                callback("Desconocido")
            }
        })
    }
}
