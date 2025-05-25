package com.example.vitalage.enfermera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.adapters.DoseHistoryAdapter
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.databinding.ActivityDoseHistoryBinding
import com.example.vitalage.model.DoseHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class DoseHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoseHistoryBinding
    private lateinit var doseHistoryAdapter: DoseHistoryAdapter
    private val doseHistoryList = mutableListOf<DoseHistory>()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var patientId: String

    private var usuarioActual: String = "Desconocido"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoseHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener nombre de usuario
        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Usuario: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Obtener datos del Intent
        patientId = intent.getStringExtra("patient_id") ?: return

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        setupRecyclerView()

        // Obtener el historial de dosis
        fetchDoseHistoryFromFirestore()


        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

    }

    private fun setupRecyclerView() {
        doseHistoryAdapter = DoseHistoryAdapter(doseHistoryList)
        binding.rvDoseHistory.layoutManager = LinearLayoutManager(this)
        binding.rvDoseHistory.adapter = doseHistoryAdapter
    }

    private fun fetchDoseHistoryFromFirestore() {
        firestore.collection("Pacientes").document(patientId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val historyRecords = document.get("historial_dosis") as? List<Map<String, Any>> ?: emptyList()

                    doseHistoryList.clear()

                    for (record in historyRecords) {
                        val medicamento = record["medicamento"] as? String ?: "Desconocido"
                        val cantidad = (record["cantidad"] as? Long)?.toInt() ?: 0
                        val dosis = (record["dosis"] as? Long)?.toInt() ?: 0
                        val fechaHora = record["fecha_hora"] as? com.google.firebase.Timestamp
                        val usuario = record["usuario"] as? String ?: "Desconocido"
                        val observaciones = record["observaciones"] as? String ?: "-"

                        doseHistoryList.add(
                            DoseHistory(medicamento, cantidad, dosis, fechaHora, usuario, observaciones)
                        )
                    }
                    doseHistoryAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener historial: ${e.message}", Toast.LENGTH_SHORT).show()
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


}
