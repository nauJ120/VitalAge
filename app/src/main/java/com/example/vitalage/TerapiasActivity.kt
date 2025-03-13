package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.adapters.TerapiaAdapter
import com.example.vitalage.clases.Terapia
import com.example.vitalage.databinding.ActivityTerapiasBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TerapiasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTerapiasBinding
    private lateinit var terapiaAdapter: TerapiaAdapter
    private val db = FirebaseFirestore.getInstance()
    private val terapiasList = mutableListOf<Terapia>()

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTerapiasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Usuario: $usuarioActual"
        }

        // Recibir datos del Intent
        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Configurar RecyclerView
        binding.recyclerTerapias.layoutManager = LinearLayoutManager(this)
        terapiaAdapter = TerapiaAdapter(terapiasList) {  }
        binding.recyclerTerapias.adapter = terapiaAdapter

        // 🔥 Se carga la lista de terapias
        fetchTerapiasFromFirestore()

        // Botón para agregar terapia
        binding.fabAddTerapia.setOnClickListener {
            val intent = Intent(this, TerapiaFormActivity::class.java)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 🔥 Se actualiza la lista de terapias al volver a la actividad
        fetchTerapiasFromFirestore()
    }

    private fun fetchTerapiasFromFirestore() {
        if (patientId.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró el residente", Toast.LENGTH_SHORT).show()
            return
        }

        val pacienteRef = db.collection("Pacientes").document(patientId)
        pacienteRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val terapias = document.get("terapias") as? List<HashMap<String, Any>> ?: emptyList()
                terapiasList.clear()

                for (terapiaData in terapias) {
                    val terapia = Terapia(
                        tipo = terapiaData["tipo"] as? String ?: "",
                        fecha = terapiaData["fecha"] as? String ?: "",
                        encargado = terapiaData["encargado"] as? String ?: "",
                        realizada = terapiaData["realizada"] as? Boolean ?: false
                    )
                    terapiasList.add(terapia)
                }
                terapiaAdapter.updateData(terapiasList)
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al obtener terapias", e)
        }
    }

    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombreUsuario = snapshot.child("nombre").value as? String
                        ?: snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido"

                    callback(nombreUsuario)
                } else {
                    callback("Desconocido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback("Desconocido")
            }
        })
    }
}
