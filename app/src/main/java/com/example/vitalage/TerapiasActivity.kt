package com.example.vitalage

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TerapiasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terapias)

        // Configurar el botón de retroceso
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish() // Cierra esta actividad y regresa a la anterior
        }

        // Configurar el Spinner de tipo de terapia
        val spinner = findViewById<Spinner>(R.id.spinner_therapy_type)
        val therapyTypes = listOf("Cardiovascular", "Neurológica", "Respiratoria", "Fisioterapia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, therapyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Configurar el campo de texto para el nombre del profesional
        val professionalName = findViewById<EditText>(R.id.et_professional_name)

        // Configurar el botón "Guardar"
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val selectedTherapy = spinner.selectedItem.toString()
            val professional = professionalName.text.toString()

            if (professional.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el nombre del profesional", Toast.LENGTH_SHORT).show()
            } else {
                // Guardar la información
                Toast.makeText(this, "Terapia guardada: $selectedTherapy por $professional", Toast.LENGTH_SHORT).show()
                // Aquí podrías agregar lógica para guardar en una base de datos
            }
        }

        // Configurar el botón "Cancelar"
        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            // Limpiar los campos o simplemente cerrar la pantalla
            professionalName.text.clear()
            spinner.setSelection(0)
            Toast.makeText(this, "Se canceló la terapia", Toast.LENGTH_SHORT).show()
        }
    }
}
