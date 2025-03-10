package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.vitalage.databinding.EscaneadoBinding


class EscaneoActivity: AppCompatActivity() {

    private lateinit var escaneadoBinding: EscaneadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escaneado)
        enableEdgeToEdge()

        val botoncancelar = findViewById<AppCompatButton>(R.id.buttonCamera)

        botoncancelar.setOnClickListener{
            val intent = Intent(this, FotoCamaraActivity::class.java)
            startActivity(intent)
        }

    }

}