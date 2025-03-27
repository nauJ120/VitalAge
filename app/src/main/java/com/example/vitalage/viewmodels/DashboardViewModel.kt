package com.example.vitalage.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.vitalage.model.SignoVital

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _totalResidents = MutableLiveData(0)
    val totalResidents: LiveData<Int> = _totalResidents

    private val _pendingAlerts = MutableLiveData(0)
    val pendingAlerts: LiveData<Int> = _pendingAlerts

    private val _avgIMC = MutableLiveData(0.0)
    val avgIMC: LiveData<Double> = _avgIMC

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        db.collection("Pacientes").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            _totalResidents.value = snapshot?.size() ?: 0
        }

        db.collection("Medicamentos")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                _pendingAlerts.value = snapshot?.size() ?: 0
            }

        db.collection("Signos Vitales").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val signos = snapshot?.toObjects(SignoVital::class.java) ?: emptyList()
            val imcValues = signos.map { it.imc }
            _avgIMC.value = if (imcValues.isNotEmpty()) imcValues.average().toDouble() else 0.0
        }
    }
}
