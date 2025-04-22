package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class PatientListActivity : AppCompatActivity() {

    private lateinit var adapter: PatientAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchField: EditText
    private val patientList = mutableListOf<Patient>()
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        // Obtener nombre de usuario actual
        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.tvUser)
            tvUser.text = "Enfermera: $usuarioActual"
        }

        recyclerView = findViewById(R.id.rv_patient_list)
        searchField = findViewById(R.id.et_search)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PatientAdapter(patientList) { patient ->
            openPatientMenu(patient)
        }
        recyclerView.adapter = adapter

        fetchPatientsFromFirestore()

        searchField.addTextChangedListener { query ->
            filterPatients(query.toString())
        }
        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@PatientListActivity, "No puedes volver atrás.", Toast.LENGTH_SHORT).show()
            }
        })
    }





    private fun fetchPatientsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                patientList.clear()
                for (document in result) {
                    val name = document.getString("nombre") ?: "Sin Nombre"
                    val id = document.id // 🔥 Asegura que obtenemos correctamente el ID del paciente
                    val gender = document.getString("sexo") ?: "No especificado"
                    val age = document.getLong("edad")?.toInt() ?: 0

                    Log.d("PatientListActivity", "Paciente cargado: ID=$id, Nombre=$name") // 🔥 Verificar en Logcat

                    patientList.add(Patient(name, id, gender, age))
                }
                adapter.updateData(patientList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar pacientes: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PatientListActivity", "Error al obtener pacientes desde Firestore", e)
            }
    }





    private fun filterPatients(query: String) {
        val filteredList = patientList.filter {
            it.name.contains(query, ignoreCase = true) || it.id.contains(query)
        }
        adapter.updateData(filteredList)
    }

    // ✅ Nueva función para abrir el menú de un paciente y pasar patient_id
    private fun openPatientMenu(patient: Patient) {
        Log.d("PatientListActivity", "Paciente seleccionado: ID=${patient.id}, Nombre=${patient.name}") // 🔥 Verificar en Logcat

        val intent = Intent(this, MenuActivity::class.java).apply {
            putExtra("patient_name", patient.name)
            putExtra("patient_id", patient.id)
            putExtra("patient_gender", patient.gender)
            putExtra("patient_age", patient.age)
        }
        startActivity(intent)
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
