package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.IniciarSesionBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


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

        val database = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var rol = ""
        if (userId != null) {
            database.child("user").child("users").child(userId).child("rol")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        rol = snapshot.value.toString()
                        Log.d("Firebase", "El rol del usuario es: $rol")
                    } else {
                        Log.d("Firebase", "No se encontró el rol")
                    }
                }
                .addOnFailureListener {
                    Log.e("Firebase", "Error al obtener el rol", it)
                }
        } else {
            Log.d("Firebase", "No hay usuario autenticado")
        }


        auth.signInWithEmailAndPassword(
            binding.correoForm.text.toString(),
            binding.contraForm.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Tu rol es:$rol", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@IniciarSesionActivity, PatientListActivity::class.java))
                finish()
            } else {

                task.exception?.localizedMessage?.let {
                    Toast.makeText(applicationContext, "Fallo de inicio de sesión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}