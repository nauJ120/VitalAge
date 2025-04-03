package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.ActivityUserFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*

class UserFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserFormBinding
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userId: String? = null
    private var oldEmail: String? = null
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID del usuario si se está editando
        userId = intent.getStringExtra("user_id")

        // Configurar spinner de roles
        val roles = listOf("Enfermera", "Médico")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRole.adapter = adapter

        // Si se edita un usuario, cargar sus datos
        userId?.let { loadUserData(it) }

        // Botón Guardar
        binding.btnSaveUser.setOnClickListener { saveUser() }

        // Botón Cancelar
        binding.btnCancel.setOnClickListener {
            finish()  // Cerrar la actividad sin guardar
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

    private fun loadUserData(userId: String) {
        val databaseRef = database.child(userId)

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val nombreUsuario = snapshot.child("nombre_usuario").getValue(String::class.java) ?: ""
                val correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                val identificacion = snapshot.child("identificacion").getValue(String::class.java) ?: ""
                val rol = snapshot.child("rol").getValue(String::class.java) ?: ""

                oldEmail = correo // Guardar el correo actual antes de editarlo

                // Establecer datos en los campos de entrada
                binding.etFullName.setText(nombreUsuario)
                binding.etEmail.setText(correo)
                binding.etIdentification.setText(identificacion)

                // Seleccionar el rol en el Spinner
                val rolIndex = listOf("Enfermera", "Médico").indexOf(rol)
                if (rolIndex >= 0) {
                    binding.spRole.setSelection(rolIndex)
                }
            } else {
                Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad si el usuario no existe
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val id = binding.etIdentification.text.toString().trim()
        val role = binding.spRole.selectedItem.toString()
        val password = binding.etPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = mapOf(
            "nombre_usuario" to fullName,
            "correo" to email,
            "identificacion" to id,
            "rol" to role
        )

        if (userId == null) {
            // 🔥 CREAR NUEVO USUARIO EN FIREBASE AUTHENTICATION
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser: FirebaseUser? = authResult.user

                    // Si se creó con éxito, guardar en Realtime Database
                    if (firebaseUser != null) {
                        database.child(firebaseUser.uid).setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Usuario agregado con éxito", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al agregar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                            }

                        // 🔹 ACTUALIZAR EL NOMBRE DEL USUARIO EN AUTH
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                        firebaseUser.updateProfile(profileUpdates)
                    }
                }
                .addOnFailureListener { e ->
                    if (e is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Este correo ya está en uso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al registrar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // 🔥 EDITAR USUARIO EXISTENTE
            database.child(userId!!).updateChildren(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // 🔥 OBTENER USUARIO ACTUAL
            val user = auth.currentUser

            if (user != null) {
                // 🔹 ACTUALIZAR CORREO SI CAMBIÓ
                if (email != oldEmail) {
                    user.updateEmail(email)
                        .addOnSuccessListener {
                            database.child(userId!!).child("correo").setValue(email)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al actualizar correo: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }

                // 🔹 ACTUALIZAR CONTRASEÑA SI SE INGRESÓ UNA NUEVA
                if (password.isNotEmpty()) {
                    user.updatePassword(password)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al actualizar contraseña: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
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
