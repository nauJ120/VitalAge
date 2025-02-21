package com.example.vitalage.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.example.vitalage.models.SignoVital
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _totalResidents = MutableStateFlow(0)
    val totalResidents = _totalResidents.asStateFlow()

    private val _pendingAlerts = MutableStateFlow(0)
    val pendingAlerts = _pendingAlerts.asStateFlow()

    private val _avgIMC = MutableStateFlow(0.0)
    val avgIMC = _avgIMC.asStateFlow()

    private val _imcData = MutableStateFlow<List<Float>>(emptyList())
    val imcData = _imcData.asStateFlow()

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        viewModelScope.launch {
            db.collection("residentes").addSnapshotListener { snapshot, _ ->
                _totalResidents.value = snapshot?.size() ?: 0
            }

            db.collection("medicamentos")
                .whereEqualTo("estado", "pendiente")
                .addSnapshotListener { snapshot, _ ->
                    _pendingAlerts.value = snapshot?.size() ?: 0
                }

            db.collection("signos_vitales").addSnapshotListener { snapshot, _ ->
                val signos = snapshot?.toObjects(SignoVital::class.java) ?: emptyList()
                val imcValues = signos.map { it.imc }

                _avgIMC.value = if (imcValues.isNotEmpty()) imcValues.average().toDouble() else 0.0
                _imcData.value = imcValues // Guardamos los datos para la gráfica
            }
        }
    }
}
