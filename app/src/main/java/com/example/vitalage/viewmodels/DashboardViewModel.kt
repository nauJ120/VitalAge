package com.example.vitalage.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.vitalage.models.Medicamento
import com.example.vitalage.models.SignoVital
import kotlinx.coroutines.flow.MutableStateFlow

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    val medicamentos = MutableStateFlow(listOf<Medicamento>())
    val signosVitales = MutableStateFlow(listOf<SignoVital>())
    init {
        obtenerMedicamentos()
        obtenerSignosVitales()
    }

    private fun obtenerMedicamentos() {
        db.collection("medicamentos").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.toObjects(Medicamento::class.java) ?: emptyList()
            medicamentos.value = lista
        }
    }
    private fun obtenerSignosVitales() {
        db.collection("historial_signos").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.toObjects(SignoVital::class.java) ?: emptyList()
            signosVitales.value = lista
        }
    }
}
