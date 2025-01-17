package com.example.vitalage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.IniciarSesionBinding

class IniciarSesionActivity : AppCompatActivity() {
    private lateinit var binding: IniciarSesionBinding
 // esto es una pruebasda skodlad sald
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = IniciarSesionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
    }

}