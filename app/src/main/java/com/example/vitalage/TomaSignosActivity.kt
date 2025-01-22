package com.example.vitalage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TomaSignosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toma_signos)

        // Referencias a los elementos del layout
        val inputFrecuenciaRespiratoria = findViewById<EditText>(R.id.inputFrecuenciaRespiratoria)
        val inputSaturacionOxigeno = findViewById<EditText>(R.id.inputSaturacionOxigeno)
        val inputPresionArterial = findViewById<EditText>(R.id.inputPresionArterial)
        val inputTemperaturaCorporal = findViewById<EditText>(R.id.inputTemperaturaCorporal)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // Botón Guardar
        btnGuardar.setOnClickListener {
            val frecuencia = inputFrecuenciaRespiratoria.text.toString()
            val saturacion = inputSaturacionOxigeno.text.toString()
            val presion = inputPresionArterial.text.toString()
            val temperatura = inputTemperaturaCorporal.text.toString()

            if (frecuencia.isNotEmpty() && saturacion.isNotEmpty() && presion.isNotEmpty() && temperatura.isNotEmpty()) {
                // Aquí puedes guardar los datos en la base de datos
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cancelar
        btnCancelar.setOnClickListener {
            finish()
        }
    }
}
