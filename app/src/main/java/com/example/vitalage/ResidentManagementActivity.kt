package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.databinding.ActivityResidentManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ResidentManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentManagementBinding
    private lateinit var patientAdapter: PatientAdapter
    private val db = FirebaseFirestore.getInstance()
    private var patientsList = mutableListOf<Patient>()
    private var patientsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView con el adaptador existente
        binding.recyclerResidents.layoutManager = LinearLayoutManager(this)
        patientAdapter = PatientAdapter(patientsList, this::editResident)
        binding.recyclerResidents.adapter = patientAdapter

        // Obtener residentes desde Firestore
        fetchResidentsFromFirestore()

        // Botón para agregar un nuevo residente
        binding.fabAddResident.setOnClickListener {
            startActivity(Intent(this, ResidentFormActivity::class.java))
        }
    }

    private fun fetchResidentsFromFirestore() {
        patientsListener = db.collection("Pacientes")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "Error al obtener residentes", error)
                    return@addSnapshotListener
                }

                patientsList.clear()
                for (document in snapshots!!) {
                    val patient = Patient(
                        name = document.getString("nombreCompleto") ?: "Desconocido",
                        id = document.id,
                        gender = document.getString("genero") ?: "No especificado",
                        age = document.getLong("edad")?.toInt() ?: 0
                    )
                    patientsList.add(patient)
                }
                patientAdapter.updateData(patientsList)
            }
    }

    private fun editResident(patient: Patient) {
        val intent = Intent(this, ResidentFormActivity::class.java).apply {
            putExtra("patient_id", patient.id)
        }
        startActivity(intent)
    }

    private fun deleteResident(patient: Patient) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar residente")
            .setMessage("¿Seguro que deseas eliminar a ${patient.name}?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("Pacientes").document(patient.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Residente eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        patientsListener?.remove()
    }
}
