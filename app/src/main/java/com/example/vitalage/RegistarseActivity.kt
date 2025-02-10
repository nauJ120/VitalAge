package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.CheckBox
import com.example.vitalage.databinding.RegistrarseBinding

class RegistarseActivityActivity : AppCompatActivity() {

    var message = "Cedula de Ciudadania"
    private lateinit var binding: RegistrarseBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = RegistrarseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        val atras = findViewById<ImageView>(R.id.flechitaatras)

        val checkb = findViewById<CheckBox>(R.id.checkbox)
        val checkbo = findViewById<CheckBox>(R.id.checkbox2)
        val checkbox3 = findViewById<CheckBox>(R.id.checkbox3)

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


        atras.setOnClickListener(){
            val intent = Intent(this, IniciarSesionActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener(){
            val intent = Intent(this, IniciarSesionActivity::class.java)
            startActivity(intent)
        }

        checkb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkbo.isChecked = false
                checkbox3.isChecked = false
            }
        }

        checkbo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkb.isChecked = false
                checkbox3.isChecked = false
            }
        }

        checkbox3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkb.isChecked = false
                checkbo.isChecked = false
            }
        }
    }

}