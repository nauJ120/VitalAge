package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import androidx.appcompat.widget.AppCompatButton


import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.IniciarSesionBinding

class IniciarSesionActivity : AppCompatActivity() {
    private lateinit var binding: IniciarSesionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = IniciarSesionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        val boton = findViewById<AppCompatButton>(R.id.button)

        val boton_registro = findViewById<AppCompatButton>(R.id.button2)

        boton_registro.setOnClickListener{
            val intent = Intent(this, RegistarseActivityActivity::class.java)
            startActivity(intent)
        }
    }

}