package com.example.vitalage

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Configurar Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"

        // Inicializar el gráfico
        barChart = findViewById(R.id.barChart)
        setupChart()
        fetchChartData()
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
            barChart.invalidate() // Refrescar gráfico
        }
    }
}
