package com.example.vitalage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddNursingNoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_nursing_note)

        val saveButton = findViewById<Button>(R.id.btn_save)
        val cancelButton = findViewById<Button>(R.id.btn_cancel)
        val patientField = findViewById<EditText>(R.id.et_patient)
        val appointmentField = findViewById<EditText>(R.id.et_appointment)
        val descriptionField = findViewById<EditText>(R.id.et_description)

        // Configurar acción del botón Guardar
        saveButton.setOnClickListener {
            val patient = patientField.text.toString()
            val appointment = appointmentField.text.toString()
            val description = descriptionField.text.toString()

            if (patient.isBlank() || appointment.isBlank() || description.isBlank()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nota de enfermería guardada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cerrar la pantalla y volver a la anterior
            }
        }
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // Configurar acción del botón Cancelar
        cancelButton.setOnClickListener {
            finish() // Cerrar la pantalla
        }
    }
}
