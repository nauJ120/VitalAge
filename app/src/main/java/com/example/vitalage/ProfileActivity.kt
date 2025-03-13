package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var userId: String? = null
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(userId!!)

        // Configurar el spinner de tipo de documento
        setupTipoDocumentoSpinner()

        // Cargar información del usuario
        loadUserData()

        // Botón Guardar Cambios (Solo Admin)
        binding.btnSaveChanges.setOnClickListener { saveChanges() }

        // Botón Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
        }
    }

    private fun setupTipoDocumentoSpinner() {
        val tiposDocumento = resources.getStringArray(R.array.Tipos_de_documentos)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposDocumento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipoDocumento.adapter = adapter
    }

    private fun loadUserData() {
        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val nombre = snapshot.child("nombre_usuario").value.toString()
                val correo = snapshot.child("correo").value.toString()
                val identificacion = snapshot.child("identificacion").value.toString()
                val tipoDocumento = snapshot.child("tipodocumento").value.toString()
                userRole = snapshot.child("rol").value.toString()

                // Mostrar datos en los campos
                binding.etFullName.setText(nombre)
                binding.etEmail.setText(correo)
                binding.etIdentification.setText(identificacion)
                binding.tvRole.text = "Rol: $userRole"

                // Seleccionar el tipo de documento en el Spinner
                val tiposDocumento = resources.getStringArray(R.array.Tipos_de_documentos)
                val tipoIndex = tiposDocumento.indexOf(tipoDocumento)
                if (tipoIndex >= 0) {
                    binding.spTipoDocumento.setSelection(tipoIndex)
                }

                // Si el usuario es administrador, permitir edición
                if (userRole == "Administrador") {
                    binding.btnSaveChanges.visibility = View.VISIBLE
                    binding.etPassword.visibility = View.VISIBLE
                    binding.tvPasswordLabel.visibility = View.VISIBLE
                    findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
                        startActivity(Intent(this, MenuAdminActivity::class.java))
                    }

                    findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else {
                    // Si es médico o enfermero, los campos son solo lectura
                    findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
                        startActivity(Intent(this, PatientListActivity::class.java))
                    }

                    findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    disableFields()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableFields() {
        binding.etFullName.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etIdentification.isEnabled = false
        binding.spTipoDocumento.isEnabled = false
    }

    private fun saveChanges() {
        val updatedName = binding.etFullName.text.toString().trim()
        val updatedEmail = binding.etEmail.text.toString().trim()
        val updatedIdentification = binding.etIdentification.text.toString().trim()
        val updatedDocumentType = binding.spTipoDocumento.selectedItem.toString().trim()
        val newPassword = binding.etPassword.text.toString().trim()

        if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedIdentification.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "nombre_usuario" to updatedName,
            "correo" to updatedEmail,
            "identificacion" to updatedIdentification,
            "tipodocumento" to updatedDocumentType
        )

        databaseRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()

            // Actualizar correo en Firebase Authentication si cambió
            if (updatedEmail != auth.currentUser?.email) {
                auth.currentUser?.updateEmail(updatedEmail)
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar el correo en autenticación", Toast.LENGTH_SHORT).show()
                    }
            }

            // Cambiar la contraseña solo si se ha ingresado una nueva
            if (newPassword.isNotEmpty()) {
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show()
        }
    }
}
