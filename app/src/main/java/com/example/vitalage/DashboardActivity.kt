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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitalage.R
import com.example.vitalage.viewmodels.DashboardViewModel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val totalResidents by viewModel.totalResidents.collectAsState()
    val pendingAlerts by viewModel.pendingAlerts.collectAsState()
    val avgIMC by viewModel.avgIMC.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.VitalAge),
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

        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            drawLine(color = Color.Blue, start = Offset(50f, 50f), end = Offset(50f, totalResidents.toFloat() * 10f), strokeWidth = 8f)
            drawLine(color = Color.Red, start = Offset(150f, 50f), end = Offset(150f, pendingAlerts.toFloat() * 10f), strokeWidth = 8f)
            drawLine(color = Color.Green, start = Offset(250f, 50f), end = Offset(250f, avgIMC.toFloat() * 10f), strokeWidth = 8f)
        }
    }
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}
