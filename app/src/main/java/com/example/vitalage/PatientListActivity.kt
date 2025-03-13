package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore

class PatientListActivity : AppCompatActivity() {

    private lateinit var adapter: PatientAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchField: EditText
    private val patientList = mutableListOf<Patient>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

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
}
