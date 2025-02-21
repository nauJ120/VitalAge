package com.example.vitalage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitalage.viewmodels.DashboardViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Color

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val totalResidents by viewModel.totalResidents.collectAsState()
    val pendingAlerts by viewModel.pendingAlerts.collectAsState()
    val avgIMC by viewModel.avgIMC.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.vital1),
            contentDescription = "Logo VitalAge",
            modifier = Modifier.size(150.dp).padding(bottom = 16.dp)
        )

        Text(text = "Dashboard", style = MaterialTheme.typography.h5)

        Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Total de Residentes: $totalResidents", style = MaterialTheme.typography.h6)
            }
        }

        Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Alertas de Medicamentos Pendientes: $pendingAlerts", style = MaterialTheme.typography.h6)
            }
        }

        Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "IMC Promedio: $avgIMC", style = MaterialTheme.typography.h6)
            }
        }

        Text(text = "Estadísticas Generales", style = MaterialTheme.typography.h6)

        BarChartView(
            totalResidents = totalResidents.toFloat(),
            pendingAlerts = pendingAlerts.toFloat(),
            avgIMC = avgIMC.toFloat()
        )
    }
}

@Composable
fun BarChartView(totalResidents: Float, pendingAlerts: Float, avgIMC: Float) {
    val context = LocalContext.current

    AndroidView(factory = { ctx ->
        BarChart(ctx).apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setTouchEnabled(true)
            setScaleEnabled(false)
            setPinchZoom(false)

            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }

            legend.isEnabled = false
        }
    }, update = { barChart ->
        val entries = listOf(
            BarEntry(0f, totalResidents),
            BarEntry(1f, pendingAlerts),
            BarEntry(2f, avgIMC)
        )

        val dataSet = BarDataSet(entries, "Estadísticas").apply {
            colors = listOf(Color.BLUE, Color.RED, Color.GREEN)
            valueTextSize = 12f
        }

        barChart.data = BarData(dataSet)
        barChart.invalidate() // Refrescar la gráfica
    })
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}
