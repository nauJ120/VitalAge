package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class NursingNotesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NursingNotesAdapter
    private val nursingNotes = mutableListOf<String>()
    private var patientId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nursing_notes)

        db = FirebaseFirestore.getInstance()

        // ✅ Obtener el ID del paciente correctamente
        patientId = intent.getStringExtra("patient_id") ?: ""

        if (patientId.isBlank()) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del paciente.", Toast.LENGTH_SHORT).show()
            Log.e("NursingNotesActivity", "patient_id está vacío o no fue recibido.")
            finish()
            return
        }

        Log.d("NursingNotesActivity", "Recibido patient_id: $patientId")

        val recyclerView = findViewById<RecyclerView>(R.id.rv_nursing_notes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NursingNotesAdapter(nursingNotes)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // ✅ Pasar el patient_id al abrir AddNursingNoteActivity
        findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            val intent = Intent(this, AddNursingNoteActivity::class.java)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
        }

        loadNursingNotes(patientId)
    }

    private fun loadNursingNotes(patientId: String) {
        db.collection("Pacientes").document(patientId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val notes = document.get("notasEnfermeria") as? List<Map<String, Any>> ?: emptyList()
                    nursingNotes.clear()
                    nursingNotes.addAll(notes.map {
                        val titulo = it["titulo"] as? String ?: "Sin título"
                        val fecha = it["fecha"] as? String ?: "Fecha desconocida"
                        val enfermera = it["enfermera"] as? String ?: "Enfermera desconocida"
                        val descripcion = it["descripcion"] as? String ?: "Sin descripción"

                        // ✅ Mostrar el título correctamente antes de la descripción
                        "📌 $titulo\n$fecha - $enfermera: $descripcion"
                    })
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar notas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
