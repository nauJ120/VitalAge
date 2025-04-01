package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.IniciarSesionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var binding: IniciarSesionBinding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = IniciarSesionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        findViewById<AppCompatButton>(R.id.button).setOnClickListener {
            iniciar_sesion()
        }

        findViewById<AppCompatButton>(R.id.button2).setOnClickListener {
            startActivity(Intent(this, RegistarseActivityActivity::class.java))
        }
    }

    private fun iniciar_sesion() {
        val email = binding.correoForm.text.toString()
        val password = binding.contraForm.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        obtenerRolYRedirigir(userId)
                    } else {
                        Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Fallo de inicio de sesión: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun obtenerRolYRedirigir(userId: String) {
        val database = FirebaseDatabase.getInstance().reference

        database.child("user").child("users").child(userId).child("rol")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val rol = snapshot.value.toString().trim()
                    Log.d("Firebase", "Rol crudo recuperado: '$rol'")

                    val intent = when (rol) {
                        "Administrador" -> Intent(this, MenuAdminActivity::class.java)
                        "Enfermera" -> Intent(this, PatientListActivity::class.java)
                        "Medico" -> Intent(this, DoctorPatientListActivity::class.java)
                        else -> {
                            Toast.makeText(this, "Rol desconocido: $rol", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                    }

                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "No se encontró el rol del usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al obtener el rol", e)
                Toast.makeText(this, "Error al obtener rol", Toast.LENGTH_SHORT).show()
            }
    }
}
