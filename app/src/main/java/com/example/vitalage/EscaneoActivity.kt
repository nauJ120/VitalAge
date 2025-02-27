package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.vitalage.databinding.EscaneadoBinding

import com.example.vitalage.databinding.FotoCamaraBinding


class EscaneoActivity: AppCompatActivity() {

    private lateinit var escaneadoBinding: EscaneadoBinding
    private val TAG = EscaneoActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        escaneadoBinding = EscaneadoBinding.inflate(layoutInflater)
        setContentView(escaneadoBinding.root)
        enableEdgeToEdge()


        val botoncancelar = findViewById<AppCompatButton>(R.id.buttonCamera)


        val nombre = intent.getStringExtra("nombreMedicamento") ?: "No detectado"
        val cantidad = intent.getStringExtra("cantidad") ?: "No detectado"
        val masa = intent.getStringExtra("masa") ?: "No detectado"
        val otrosDatos = intent.getStringExtra("otrosDatos") ?: "No detectado"


        escaneadoBinding.nombreMedicamento.text = "Nombre: $nombre\nCantidad: $cantidad\nMasa: $masa\nOtros: $otrosDatos"




        botoncancelar.setOnClickListener{
            val intent = Intent(this, FotoCamaraActivity::class.java)
            startActivity(intent)
        }

    }

}