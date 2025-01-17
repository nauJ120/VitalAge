package com.example.vitalage

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NursingNotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nursing_notes)

        // Configurar el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_nursing_notes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Datos simulados para las notas de enfermería
        val notes = listOf(
            "27/10/2024 7:00am Paciente presenta síntomas de ...",
            "25/10/2024 6:00pm Paciente presenta alteración de ...",
            "20/10/2024 3:00pm Paciente con signos vitales está ...",
            "15/10/2024 3:00pm Paciente sin novedad, signos ...",
            "10/10/2024 4:00pm Paciente presenta signos ..."
        )
        val adapter = NotesAdapter(notes)
        recyclerView.adapter = adapter

        // Acción para el botón flotante
        findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            // Implementar la funcionalidad para agregar nueva nota
        }

        // Acción para el botón de retroceso
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish() // Finaliza la actividad y regresa
        }
    }
}
