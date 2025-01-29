package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SignosVitalesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signos_vitales)

        // Botones
        val btnTomaSignos = findViewById<LinearLayout>(R.id.btnTomaSignos)
        val btnHistorial = findViewById<LinearLayout>(R.id.btnHistorial)

        // Navegación a la pantalla de toma de signos vitales
        btnTomaSignos.setOnClickListener {
            val intent = Intent(this, TomaSignosActivity::class.java)
            startActivity(intent)
        }

        // Navegación a la pantalla de historial
        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialFechaActivity::class.java)
            startActivity(intent)
        }
    }
}
