package com.example.vitalage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.ActivityResidentFormBinding
import com.google.firebase.firestore.FirebaseFirestore

class ResidentFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentFormBinding
    private val db = FirebaseFirestore.getInstance()
    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getStringExtra("patient_id")

        // Si se recibe un ID, cargar datos del paciente
        if (patientId != null) {
            loadResidentData(patientId!!)
        }

        binding.btnSaveResident.setOnClickListener {
            saveResident()
        }
    }

    private fun loadResidentData(id: String) {
        db.collection("Pacientes").document(id).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etResidentName.setText(document.getString("nombreCompleto"))
                    binding.etResidentAge.setText(document.getLong("edad")?.toString() ?: "")
                    binding.etResidentGender.setText(document.getString("genero"))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveResident() {
        val name = binding.etResidentName.text.toString().trim()
        val age = binding.etResidentAge.text.toString().trim().toIntOrNull() ?: 0
        val gender = binding.etResidentGender.text.toString().trim()

        if (name.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val patientData = mapOf(
            "nombreCompleto" to name,
            "edad" to age,
            "genero" to gender
        )

        if (patientId == null) {
            // Agregar nuevo paciente
            db.collection("Pacientes").add(patientData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Residente agregado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Editar paciente existente
            db.collection("Pacientes").document(patientId!!)
                .set(patientData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Residente actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
