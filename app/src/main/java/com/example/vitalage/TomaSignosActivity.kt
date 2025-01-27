package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TomaSignosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toma_signos)

        // Elementos del layout
        val inputFrecuenciaRespiratoria = findViewById<EditText>(R.id.inputFrecuenciaRespiratoria)
        val inputSaturacionOxigeno = findViewById<EditText>(R.id.inputSaturacionOxigeno)
        val inputPresionArterial = findViewById<EditText>(R.id.inputPresionArterial)
        val inputTemperaturaCorporal = findViewById<EditText>(R.id.inputTemperaturaCorporal)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)

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

        // Acciones para el menú desplegable
        menuIcon.setOnClickListener {
            showPopupMenu(menuIcon)
        }
    }

    private fun showPopupMenu(anchor: ImageView) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_toma_signos -> {
                    Toast.makeText(this, "Ya estás en Toma de Signos Vitales", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_item_historial_fecha -> {
                    val intent = Intent(this, HistorialFechaActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_historial_signos -> {
                    val intent = Intent(this, HistorialSignosActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_signos_vitales -> {
                    val intent = Intent(this, SignosVitalesActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
