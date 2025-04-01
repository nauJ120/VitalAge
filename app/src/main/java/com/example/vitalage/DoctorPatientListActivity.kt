package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class DoctorPatientListActivity : AppCompatActivity() {

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
            openPatientReport(patient)
        }
        recyclerView.adapter = adapter

        fetchPatientsFromFirestore()

        searchField.addTextChangedListener { query ->
            filterPatients(query.toString())
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, DoctorPatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
                    val id = document.id
                    val gender = document.getString("sexo") ?: "No especificado"
                    val age = document.getLong("edad")?.toInt() ?: 0

                    patientList.add(Patient(name, id, gender, age))
                }
                adapter.updateData(patientList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar pacientes: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DoctorPatientList", "Error al obtener pacientes desde Firestore", e)
            }
    }

    private fun filterPatients(query: String) {
        val filteredList = patientList.filter {
            it.name.contains(query, ignoreCase = true) || it.id.contains(query)
        }
        adapter.updateData(filteredList)
    }

    private fun openPatientReport(patient: Patient) {
        val intent = Intent(this, PatientReportActivity::class.java).apply {
            putExtra("patient_name", patient.name)
            putExtra("patient_id", patient.id)
            putExtra("patient_gender", patient.gender)
            putExtra("patient_age", patient.age)
        }
        startActivity(intent)
    }
}
