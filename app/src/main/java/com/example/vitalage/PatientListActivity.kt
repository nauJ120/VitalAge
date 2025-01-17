package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener

class PatientListActivity : AppCompatActivity() {

    private val patients = listOf(
        Patient("Bryan Caicedo", "123456789", "Hombre", 47),
        Patient("Jose Luis Molina", "123456789", "Hombre", 75),
        Patient("Sofía Bolívar Arévalo", "123456789", "Mujer", 82)
    )

    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_patient_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PatientAdapter(patients) { patient ->
            // Redirigir a la pantalla de Menú
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("patient_name", patient.name)
            intent.putExtra("patient_id", patient.id)
            intent.putExtra("patient_gender", patient.gender)
            intent.putExtra("patient_age", patient.age)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Filtrar pacientes
        val searchField = findViewById<EditText>(R.id.et_search)
        searchField.addTextChangedListener { editable ->
            val query = editable?.toString() ?: ""
            val filteredPatients = patients.filter {
                it.name.contains(query, ignoreCase = true) || it.id.contains(query)
            }
            adapter.updateData(filteredPatients)
        }
    }
}
