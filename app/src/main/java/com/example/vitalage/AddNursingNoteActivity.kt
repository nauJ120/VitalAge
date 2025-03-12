package com.example.vitalage

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNursingNoteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_nursing_note)

        db = FirebaseFirestore.getInstance()

        // ✅ Obtener el ID del paciente
        patientId = intent.getStringExtra("patient_id") ?: ""

        if (patientId.isBlank()) {
            Toast.makeText(this, "Error: No se recibió el ID del paciente.", Toast.LENGTH_SHORT).show()
            Log.e("AddNursingNoteActivity", "patient_id está vacío o no fue recibido.")
            finish()
            return
        }

        Log.d("AddNursingNoteActivity", "Recibido patient_id: $patientId")

        val saveButton = findViewById<Button>(R.id.btn_save)
        val cancelButton = findViewById<Button>(R.id.btn_cancel)
        val descriptionField = findViewById<EditText>(R.id.et_description)

        val enfermera = "NombreEnfermera" // Se puede obtener desde FirebaseAuth

        saveButton.setOnClickListener {
            val descripcion = descriptionField.text.toString()
            saveNursingNote(patientId, enfermera, descripcion)
        }

        cancelButton.setOnClickListener { finish() }
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }
    }

    private fun saveNursingNote(patientId: String, enfermera: String, descripcion: String) {
        if (descripcion.isBlank()) {
            Toast.makeText(this, "La descripción no puede estar vacía.", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val newNote = hashMapOf(
            "fecha" to fechaHora,
            "enfermera" to enfermera,
            "descripcion" to descripcion
        )

        val patientRef = db.collection("Pacientes").document(patientId)

        patientRef.update("notasEnfermeria", FieldValue.arrayUnion(newNote))
            .addOnSuccessListener {
                Toast.makeText(this, "Nota guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar la nota: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
