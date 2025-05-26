package com.example.vitalage.enfermera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.adapters.SignosVitalesAdapter
import com.example.vitalage.databinding.ActivitySignosVitalesBinding
import com.example.vitalage.generales.IniciarSesionActivity
import com.example.vitalage.model.SignosVitales
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignosVitalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignosVitalesBinding
    private lateinit var signosAdapter: SignosVitalesAdapter
    private val signosList = mutableListOf<SignosVitales>()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var encargado: String

    private lateinit var patientId: String
    private lateinit var patientName: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignosVitalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del intent
        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: "Paciente"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Setear info en pantalla
        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Botón de regreso

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Footer navegación
        binding.btnHomeContainer.setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }
        binding.btnProfileContainer.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Configurar RecyclerView
        signosAdapter = SignosVitalesAdapter(signosList)
        binding.recyclerSignosVitales.apply {
            layoutManager = LinearLayoutManager(this@SignosVitalesActivity)
            adapter = signosAdapter
        }

        // Botón agregar nueva toma
        binding.fabAgregarSignos.setOnClickListener {
            val intent = Intent(this, SignosVitalesFormActivity::class.java)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_gender", patientGender)
            intent.putExtra("patient_age", patientAge)
            startActivity(intent)
        }

        binding.fabSecundario.setOnClickListener {
            val intent = Intent(this, GraficasSignosActivity::class.java)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_gender", patientGender)
            intent.putExtra("patient_age", patientAge)
            startActivity(intent)
        }

        obtenerSignosVitales()

        obtenerNombreUsuario { nombre ->
            encargado = nombre
            binding.tvSubtitulo.text = "Enfermera: $encargado"

        }
    }

    private fun obtenerSignosVitales() {
        firestore.collection("Pacientes").document(patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val signosFirestore = snapshot.get("signos_vitales") as? List<Map<String, Any>> ?: emptyList()
                    signosList.clear()

                    for (registro in signosFirestore) {
                        try {
                            val signo = SignosVitales(
                                fecha = registro["fecha"] as? String ?: "",
                                frecuenciaCardiaca = (registro["frecuencia_cardiaca"] as? Number)?.toInt() ?: 0,
                                frecuenciaRespiratoria = (registro["frecuencia_respiratoria"] as? Number)?.toInt() ?: 0,
                                saturacionOxigeno = (registro["saturacion_oxigeno"] as? Number)?.toInt() ?: 0,
                                presionArterial = (registro["presion_arterial"] as? Number)?.toInt() ?: 0,
                                temperatura = (registro["temperatura"] as? Number)?.toDouble() ?: 0.0,
                                peso = (registro["peso"] as? Number)?.toDouble() ?: 0.0,
                                imc = (registro["imc"] as? Number)?.toDouble() ?: 0.0,
                                encargado = registro["encargado"] as? String ?: "",
                                escalaDolor = (registro["escala_dolor"] as? Number)?.toInt() ?: 0
                            )
                            signosList.add(signo)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    signosAdapter.notifyDataSetChanged()
                }
            }
    }


    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        Log.d("Firebase", "UID del usuario autenticado: $uid")

        // 🔥 Corregimos la referencia según la estructura: user -> users -> {UID}
        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Intentamos obtener el nombre desde ambas posibles claves
                    val nombreUsuario = snapshot.child("nombre").value as? String
                        ?: snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido"

                    Log.d("Firebase", "Nombre obtenido de la base de datos: $nombreUsuario")
                    callback(nombreUsuario)
                } else {
                    Log.e("Firebase", "No se encontró el usuario en la base de datos.")
                    callback("Desconocido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener el nombre: ${error.message}")
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
