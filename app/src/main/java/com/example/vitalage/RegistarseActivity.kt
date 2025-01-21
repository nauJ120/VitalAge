package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.text.InputType

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText


import com.example.vitalage.databinding.RegistrarseBinding

class RegistarseActivityActivity : AppCompatActivity() {

    var message = "Cedula de Ciudadania"
    private lateinit var binding: RegistrarseBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = RegistrarseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        val editable = findViewById<TextInputEditText>(R.id.tipo_de_doc)
        editable.isFocusable = false
        editable.isFocusableInTouchMode = false
        editable.inputType = InputType.TYPE_NULL

        message = intent.getStringExtra("tipo_documento") ?: message
        editable.setText(message)

        editable.setOnClickListener(){
            val intent = Intent(this, TipoDocumentoActivity::class.java)
            startActivity(intent)
        }
    }

}