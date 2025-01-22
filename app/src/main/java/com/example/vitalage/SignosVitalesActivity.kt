package com.example.vitalage

import android.content.Intent
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
// Hacer menu desplegable
            //val intent = Intent(this, MenuActivity::class.java)
            //startActivity(intent)
        }
    }
}
