package com.example.vitalage

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val emailFromIntent = intent.getStringExtra("email")

        if (emailFromIntent != "cramirez@gmail.com") {
            Toast.makeText(this, "Acceso denegado. Solo el administrador puede ingresar.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        // Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"

        // Gráfico
        barChart = findViewById(R.id.barChart)
        setupChart()
        fetchChartData()

        // Botón cerrar sesión
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupChart() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setPinchZoom(true)

        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)

        val rightAxis: YAxis = barChart.axisRight
        rightAxis.isEnabled = false

        barChart.legend.isEnabled = true
    }

    private fun fetchChartData() {
        db.collection("signos_vitales").get().addOnSuccessListener { snapshot ->
            val signos = snapshot.documents
            val entries = ArrayList<BarEntry>()

            signos.forEachIndexed { index, doc ->
                val imc = doc.getDouble("imc") ?: 0.0
                entries.add(BarEntry(index.toFloat(), imc.toFloat()))
            }

            val dataSet = BarDataSet(entries, "IMC Promedio")
            dataSet.color = Color.BLUE
            dataSet.valueTextColor = Color.BLACK

            val barData = BarData(dataSet)
            barChart.data = barData
            barChart.invalidate()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }
}
