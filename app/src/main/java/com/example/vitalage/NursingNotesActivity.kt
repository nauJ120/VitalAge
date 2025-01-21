package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NursingNotesActivity : AppCompatActivity() {

    private lateinit var adapter: NursingNotesAdapter
    private val nursingNotes = mutableListOf(
        "27/10/2024 7:00am Paciente presenta síntomas de ...",
        "25/10/2024 6:00pm Paciente presenta alteración de ...",
        "20/10/2024 3:00pm Paciente con signos vitales esta...",
        "15/10/2024 3:00pm Paciente sin novedad, signos ...",
        "10/10/2024 4:00pm Paciente presenta signos ...",
        "05/10/2024 12:00pm Paciente estable, sin dolor...",
        "30/09/2024 12:00am Paciente con alteración de ..."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nursing_notes)

        // Configurar botón de retroceso
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_nursing_notes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NursingNotesAdapter(nursingNotes)
        recyclerView.adapter = adapter

        // Configurar campo de búsqueda con TextWatcher
        val searchField = findViewById<EditText>(R.id.et_search)
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No hacemos nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                val filteredNotes = nursingNotes.filter { it.contains(query, ignoreCase = true) }
                adapter.updateNotes(filteredNotes)
            }

            override fun afterTextChanged(s: Editable?) {
                // No hacemos nada aquí
            }
        })

        // Botón flotante para agregar nueva nota
        findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            val intent = Intent(this, AddNursingNoteActivity::class.java)
            startActivity(intent)
        }
    }
}
