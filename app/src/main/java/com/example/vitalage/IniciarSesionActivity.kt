package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.IniciarSesionBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth




class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var binding: IniciarSesionBinding
    private var auth: FirebaseAuth = Firebase.auth



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

        boton.setOnClickListener{
            iniciar_sesion()
        }

    }

    private fun iniciar_sesion() {
        auth.signInWithEmailAndPassword(
            binding.correoForm.text.toString(),
            binding.contraForm.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this@IniciarSesionActivity, PatientListActivity::class.java))
                finish()

            } else {
                // If sign in fails, display a message to the user.
                task.exception?.localizedMessage?.let {
                    Toast.makeText(applicationContext, "Fallo de inicio de sesión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}