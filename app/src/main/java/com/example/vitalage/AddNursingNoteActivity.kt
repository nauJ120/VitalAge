package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNursingNoteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var patientId: String
    private lateinit var encargado: String // Nombre del enfermero/a
    private lateinit var saveButton: Button // Botón Guardar
    private lateinit var titleField: EditText // ✅ Campo de título
    private lateinit var descriptionField: EditText // ✅ Campo de descripción
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_nursing_note)

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        db = FirebaseFirestore.getInstance()

        // Obtener el ID del paciente
        patientId = intent.getStringExtra("patient_id") ?: ""

        if (patientId.isBlank()) {
            Toast.makeText(this, "Error: No se recibió el ID del paciente.", Toast.LENGTH_SHORT).show()
            Log.e("AddNursingNoteActivity", "patient_id está vacío o no fue recibido.")
            finish()
            return
        }

        Log.d("AddNursingNoteActivity", "Recibido patient_id: $patientId")

        // Inicializar vistas
        saveButton = findViewById(R.id.btn_save)
        val cancelButton = findViewById<Button>(R.id.btn_cancel)

        titleField = findViewById(R.id.et_title) // ✅ Campo de título
        descriptionField = findViewById(R.id.et_description)

        // Deshabilitar el botón Guardar hasta que se obtenga el nombre del usuario
        saveButton.isEnabled = false

        // ✅ Obtener el nombre del usuario logueado (enfermero/a)
        obtenerNombreUsuario { nombre ->
            encargado = nombre
            Log.d("AddNursingNoteActivity", "Nombre de enfermera asignado: $encargado")
            saveButton.isEnabled = true // Habilitar botón guardar después de obtener el nombre
        }

        // Acción del botón Guardar
        saveButton.setOnClickListener {
            val titulo = titleField.text.toString().trim()
            val descripcion = descriptionField.text.toString().trim()

            if (titulo.isBlank()) {
                Toast.makeText(this, "Por favor ingrese un título válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (descripcion.isBlank()) {
                Toast.makeText(this, "Por favor ingrese una descripción válida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveNursingNote(patientId, titulo, encargado, descripcion)
        }

        cancelButton.setOnClickListener { finish() }

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.tvSubtitulo)
            tvUser.text = "Enfermera: $usuarioActual"
        }
        // Botón de retroceso
        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        // ✅ Agregar Footer de Navegación
        setupFooterNavigation()
    }

    private fun saveNursingNote(patientId: String, titulo: String, enfermera: String, descripcion: String) {
        val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val newNote = hashMapOf(
            "titulo" to titulo, // ✅ Guardar el título en Firestore
            "fecha" to fechaHora,
            "enfermera" to enfermera,
            "descripcion" to descripcion
        )

        val patientRef = db.collection("Pacientes").document(patientId)

        patientRef.update("notasEnfermeria", FieldValue.arrayUnion(newNote))
            .addOnSuccessListener {
                Toast.makeText(this, "Nota guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar la nota: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ✅ Función para obtener el nombre del usuario logueado desde la base de datos
    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        Log.d("Firebase", "UID del usuario autenticado: $uid")

        // 🔥 Corregimos la referencia para que apunte a la estructura correcta: user/users/{UID}
        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Datos recibidos desde Firebase: ${snapshot.value}") // 🔥 Ver qué datos llegan

                if (snapshot.exists()) {
                    val nombreUsuario = snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido" // ✅ Ahora buscamos específicamente en "nombre_usuario"

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

    // ✅ Configurar Footer de Navegación
    private fun setupFooterNavigation() {
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        val btnProfile = findViewById<ImageView>(R.id.btnProfile)

        btnHome.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnProfile.setOnClickListener {
            Toast.makeText(this, "Perfil en construcción", Toast.LENGTH_SHORT).show()
        }
    }
}
