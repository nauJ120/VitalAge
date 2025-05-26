package com.example.vitalage.enfermera

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.R
import com.example.vitalage.databinding.ActivitySignosVitalesFormBinding
import com.example.vitalage.generales.IniciarSesionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

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
            val presionDiastolica = binding.etPresionDiastolica.text.toString().toInt()
            val presionSistolica = binding.etPresionSistolica.text.toString().toInt()
            val temperatura = binding.etTemperatura.text.toString().toDouble()
            val escalaDolor = binding.etDolor.text.toString().toInt()

            val pesoText = binding.etPeso.text.toString()
            val imcText = binding.etIMC.text.toString()

            val peso = if (pesoText.isNotBlank()) pesoText.toDoubleOrNull() else null
            val imc = if (imcText.isNotBlank()) imcText.toDoubleOrNull() else null


            val presionMedia = (presionSistolica + 2 * presionDiastolica) / 3.0
            val presionArterialTexto: Int = presionMedia.roundToInt()

            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val nuevoRegistro = mutableMapOf<String, Any>(
                "fecha" to fechaActual,
                "frecuencia_cardiaca" to frecuenciaCardiaca,
                "frecuencia_respiratoria" to frecuenciaRespiratoria,
                "saturacion_oxigeno" to saturacionOxigeno,
                "presion_arterial" to presionArterialTexto,
                "temperatura" to temperatura,
                "escala_dolor" to escalaDolor,
                "encargado" to usuarioActual
            )

            peso?.let { nuevoRegistro["peso"] = it }
            imc?.let { nuevoRegistro["imc"] = it }

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
            Toast.makeText(this, "Por favor llena todos los campos obligatorios correctamente", Toast.LENGTH_SHORT).show()
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

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            // Usuario no logeado
            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        val userId = user.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("user/users/$userId")

        dbRef.get().addOnSuccessListener { snapshot ->
            val rol = snapshot.child("rol").value.toString()

            // Aquí comparas según el rol esperado
            if (rol != "Enfermera") {
                Toast.makeText(this, "Acceso solo para enfermeras", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar el rol", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
        }
    }
}
