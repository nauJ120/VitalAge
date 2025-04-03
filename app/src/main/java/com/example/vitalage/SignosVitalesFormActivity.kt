package com.example.vitalage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.ActivitySignosVitalesFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SignosVitalesFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignosVitalesFormBinding
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var patientId: String
    private lateinit var usuarioActual: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignosVitalesFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getStringExtra("patient_id") ?: ""

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvSubtitulo.text = "Enfermera: $usuarioActual"

        }

        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        // Guardar
        binding.btnConfirm.setOnClickListener {
            guardarSignosVitales()
        }

        // Cancelar
        binding.btnCancel.setOnClickListener {
            finish()
        }

    }

    private fun guardarSignosVitales() {
        try {
            val frecuenciaCardiaca = binding.etFrecuenciaCardiaca.text.toString().toInt()
            val frecuenciaRespiratoria = binding.etFrecuenciaRespiratoria.text.toString().toInt()
            val saturacionOxigeno = binding.etSaturacionOxigeno.text.toString().toInt()
            val presionArterial = binding.etPresionArterial.text.toString()
            val temperatura = binding.etTemperatura.text.toString().toDouble()
            val peso = binding.etPeso.text.toString().toDouble()
            val imc = binding.etIMC.text.toString().toDouble()

            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val nuevoRegistro = mapOf(
                "fecha" to fechaActual,
                "frecuencia_cardiaca" to frecuenciaCardiaca,
                "frecuencia_respiratoria" to frecuenciaRespiratoria,
                "saturacion_oxigeno" to saturacionOxigeno,
                "presion_arterial" to presionArterial,
                "temperatura" to temperatura,
                "peso" to peso,
                "imc" to imc,
                "encargado" to usuarioActual
            )

            val docRef = firestore.collection("Pacientes").document(patientId)
            docRef.get().addOnSuccessListener { document ->
                val signosPrevios = document.get("signos_vitales") as? MutableList<Map<String, Any>> ?: mutableListOf()
                signosPrevios.add(nuevoRegistro)

                docRef.update("signos_vitales", signosPrevios)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Signos vitales registrados", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar signos", Toast.LENGTH_SHORT).show()
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Por favor llena todos los campos correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return callback("Desconocido")
        val ref = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").value as? String
                    ?: snapshot.child("nombre_usuario").value as? String
                    ?: "Desconocido"
                callback(nombre)
            }

            override fun onCancelled(error: DatabaseError) {
                callback("Desconocido")
            }
        })
    }
}
