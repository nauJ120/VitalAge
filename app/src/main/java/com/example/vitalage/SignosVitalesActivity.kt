package com.example.vitalage

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SignosVitalesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signos_vitales)

        // Elementos del layout
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)

        // Acciones para el menú desplegable
        menuIcon.setOnClickListener {
            // Aquí puedes abrir un menú o realizar alguna acción
            // Por ejemplo:
            // val intent = Intent(this, MenuActivity::class.java)
            // startActivity(intent)
        }
    }
}
