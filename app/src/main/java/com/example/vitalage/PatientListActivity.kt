package com.example.vitalage

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
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
            Toast.makeText(this, "Seleccionaste a ${patient.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

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
