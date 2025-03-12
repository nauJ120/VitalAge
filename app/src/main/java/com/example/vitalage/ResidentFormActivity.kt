package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.ActivityResidentFormBinding
import com.google.firebase.firestore.FirebaseFirestore

class ResidentFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentFormBinding
    private val db = FirebaseFirestore.getInstance()
    private var residentId: String? = null // ID del residente (cedula)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID del residente (Si es edición)
        residentId = intent.getStringExtra("resident_id")

        if (residentId != null) {
            cargarDatosResidente(residentId!!)
        }

        // Guardar o actualizar residente
        binding.btnSaveResident.setOnClickListener {
            guardarResidente()
        }

        binding.btnCancel.setOnClickListener {
            finish() // Cierra la actividad
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun cargarDatosResidente(id: String) {
        db.collection("Pacientes").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etResidentName.setText(document.getString("nombre"))
                    binding.etResidentId.setText(id) // La cédula es el ID
                    binding.etResidentAge.setText(document.getLong("edad")?.toString() ?: "")
                    binding.etResidentGender.setText(document.getString("sexo") ?: "")

                    // Deshabilitamos la edición del ID (cedula) porque no debe cambiarse
                    binding.etResidentId.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar residente", e)
                Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarResidente() {
        val nombre = binding.etResidentName.text.toString().trim()
        val cedula = binding.etResidentId.text.toString().trim() // Ahora usamos la cédula como ID
        val edad = binding.etResidentAge.text.toString().trim().toIntOrNull() ?: 0
        val genero = binding.etResidentGender.text.toString().trim()

        if (nombre.isEmpty() || cedula.isEmpty() || genero.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val residente = mapOf(
            "nombre" to nombre,
            "edad" to edad,
            "sexo" to genero
        )

        db.collection("Pacientes").document(cedula) // 🔥 La cédula ahora es el ID del documento
            .set(residente)
            .addOnSuccessListener {
                Toast.makeText(this, "Residente guardado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad después de guardar
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar residente", e)
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
            }
    }
}
