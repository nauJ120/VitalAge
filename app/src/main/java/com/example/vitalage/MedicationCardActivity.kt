package com.example.vitalage

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.databinding.ActivityMedicationCardBinding
import com.example.vitalage.model.MedicationCard
import com.example.vitalage.model.SpaceItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MedicationCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationCardBinding
    private lateinit var medicationAdapter: MedicationCardAdapter
    private val medicationList = mutableListOf<MedicationCard>()

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Usuario: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        // Recibir datos del Intent
        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Mostrar datos del paciente
        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Configurar RecyclerView
        setupRecyclerView()

        binding.rvMedications.addItemDecoration(SpaceItemDecoration(16)) // 16dp de espacio entre elementos


        // Obtener los medicamentos del paciente desde Firestore
        fetchMedicationsFromFirestore()

        val btnHomeContainer = findViewById<LinearLayout>(R.id.btnHomeContainer)

        btnHomeContainer.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java) // Reemplaza "NuevaActividad" con el nombre de tu actividad destino
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationCardAdapter(medicationList)
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter
    }

    private fun fetchMedicationsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val patientRef = db.collection("Pacientes").document(patientId)

        patientRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val medicamentosPaciente = document.get("medicamentos") as? List<Map<String, Any>> ?: emptyList()

                medicationList.clear()

                for (med in medicamentosPaciente) {
                    val medId = med["medicamento_id"] as? String ?: continue

                    // Paso 2: Buscar los datos generales en la colección global
                    db.collection("Medicamentos").document(medId).get()
                        .addOnSuccessListener { globalDoc ->
                            if (globalDoc.exists()) {
                                val nombre = globalDoc.getString("nombre") ?: "Desconocido"
                                val dosis = "${globalDoc.getLong("dosis")?.toInt() ?: 0} mg"
                                val observaciones = globalDoc.getString("observaciones") ?: "N/A"

                                val mañana = med["hora_mañana"] as? String ?: "-"
                                val tarde = med["hora_tarde"] as? String ?: "-"
                                val noche = med["hora_noche"] as? String ?: "-"

                                medicationList.add(
                                    MedicationCard(
                                        nombre,
                                        dosis,
                                        observaciones,
                                        mañana,
                                        tarde,
                                        noche
                                    )
                                )
                                medicationAdapter.notifyDataSetChanged() // Se notifica por cada uno porque son async
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error cargando datos del medicamento: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Paciente no encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al obtener medicamentos: ${e.message}", Toast.LENGTH_SHORT).show()
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






