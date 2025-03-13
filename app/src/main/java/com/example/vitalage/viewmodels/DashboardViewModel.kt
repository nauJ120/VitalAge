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
            val imcValues = signos.map { it.imc.toDouble() }
            _avgIMC.value = if (imcValues.isNotEmpty()) imcValues.average() else 0.0
        }
    }
}
