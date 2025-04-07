package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.ActivityResidentFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ResidentFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentFormBinding
    private val db = FirebaseFirestore.getInstance()
    private var residentId: String? = null // ID del residente (cedula)
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID del residente (Si es edición)
        residentId = intent.getStringExtra("resident_id")

        if (residentId != null) {
            cargarDatosResidente(residentId!!)
        }

        // Guardar o actualizar residente
        binding.btnSaveResident.setOnClickListener {
            guardarResidente()
        }

        binding.btnCancel.setOnClickListener {
            finish() // Cierra la actividad
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.tvUser)
            tvUser.text = "Administrador: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosResidente(id: String) {
        db.collection("Pacientes").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etResidentName.setText(document.getString("nombre"))
                    binding.etResidentId.setText(id) // La cédula es el ID
                    binding.etResidentAge.setText(document.getLong("edad")?.toString() ?: "")
                    binding.etResidentGender.setText(document.getString("sexo") ?: "")

                    // Deshabilitamos la edición del ID (cedula) porque no debe cambiarse
                    binding.etResidentId.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar residente", e)
                Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarResidente() {
        val nombre = binding.etResidentName.text.toString().trim()
        val cedula = binding.etResidentId.text.toString().trim() // Ahora usamos la cédula como ID
        val edad = binding.etResidentAge.text.toString().trim().toIntOrNull() ?: 0
        val genero = binding.etResidentGender.text.toString().trim()

        if (nombre.isEmpty() || cedula.isEmpty() || genero.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val residente = mapOf(
            "nombre" to nombre,
            "edad" to edad,
            "sexo" to genero
        )

        db.collection("Pacientes").document(cedula) // 🔥 La cédula ahora es el ID del documento
            .set(residente)
            .addOnSuccessListener {
                Toast.makeText(this, "Residente guardado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad después de guardar
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar residente", e)
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        Log.d("Firebase", "UID del usuario autenticado: $uid")

        // 🔥 Corregimos la referencia según la estructura: user -> users -> {UID}
        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Intentamos obtener el nombre desde ambas posibles claves
                    val nombreUsuario = snapshot.child("nombre").value as? String
                        ?: snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido"

                    Log.d("Firebase", "Nombre obtenido de la base de datos: $nombreUsuario")
                    callback(nombreUsuario)
                } else {
                    Log.e("Firebase", "No se encontró el usuario en la base de datos.")
                    callback("Desconocido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener el nombre: ${error.message}")
                callback("Desconocido")
            }
        })
    }
}
