package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.vitalage.model.SignoVital
import com.example.vitalage.viewmodels.DashboardViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var barChart: BarChart

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

        // ViewModel
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        // Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"

        // UI Components
        val tvTotalResidents = findViewById<TextView>(R.id.tvTotalResidents)
        val tvPendingAlerts = findViewById<TextView>(R.id.tvPendingAlerts)
        val tvAvgIMC = findViewById<TextView>(R.id.tvAvgIMC)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        barChart = findViewById(R.id.barChart)

        setupChart()
        cargarGraficaIMC()

        // Observers
        viewModel.totalResidents.observe(this, Observer {
            tvTotalResidents.text = "Total de Residentes: $it"
        })

        viewModel.pendingAlerts.observe(this, Observer {
            tvPendingAlerts.text = "Alertas de Medicamentos Pendientes: $it"
        })

        viewModel.avgIMC.observe(this, Observer {
            tvAvgIMC.text = "IMC Promedio: %.2f".format(it)
        })

        btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
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

    private fun cargarGraficaIMC() {
        FirebaseFirestore.getInstance().collection("Signos Vitales")
            .get()
            .addOnSuccessListener { snapshot ->
                val signos = snapshot.toObjects(SignoVital::class.java)
                val entries = ArrayList<BarEntry>()

                signos.forEachIndexed { index, signo ->
                    entries.add(BarEntry(index.toFloat(), signo.imc.toFloat()))
                }

                val dataSet = BarDataSet(entries, "IMC por paciente")
                dataSet.color = ContextCompat.getColor(this, R.color.md_indigo_700)

                val barData = BarData(dataSet)
                barChart.data = barData
                barChart.invalidate()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo cargar la gráfica", Toast.LENGTH_SHORT).show()
            }
    }
}
