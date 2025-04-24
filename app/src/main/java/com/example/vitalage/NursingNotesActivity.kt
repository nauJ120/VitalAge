package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class NursingNotesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NursingNotesAdapter
    private lateinit var searchField: EditText
    private val nursingNotes = mutableListOf<Map<String, Any>>() // Lista filtrada de notas
    private val allNotes = mutableListOf<Map<String, Any>>() // Lista completa de notas
    private var patientId: String = ""
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nursing_notes)

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

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        db = FirebaseFirestore.getInstance()

        // ✅ Obtener el ID del paciente correctamente
        patientId = intent.getStringExtra("patient_id") ?: ""

        if (patientId.isBlank()) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del paciente.", Toast.LENGTH_SHORT).show()
            Log.e("NursingNotesActivity", "patient_id está vacío o no fue recibido.")
            finish()
            return
        }

        Log.d("NursingNotesActivity", "Recibido patient_id: $patientId")

        val recyclerView = findViewById<RecyclerView>(R.id.rv_nursing_notes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NursingNotesAdapter(nursingNotes, patientId) {
            loadNursingNotes(patientId) // ✅ Recargar la lista después de eliminar
        }
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // ✅ Barra de búsqueda funcional
        searchField = findViewById(R.id.et_search)
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString()) // 🔥 Filtrar notas en tiempo real
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            val intent = Intent(this, AddNursingNoteActivity::class.java)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
        }

        loadNursingNotes(patientId)

        // ✅ Agregar Footer de Navegación
        setupFooterNavigation()
    }

    override fun onResume() {
        super.onResume()
        searchField.setText("") // 🔥 Limpiar búsqueda al regresar
        loadNursingNotes(patientId)
    }

    private fun loadNursingNotes(patientId: String) {
        db.collection("Pacientes").document(patientId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val notes = document.get("notasEnfermeria") as? List<Map<String, Any>> ?: emptyList()
                    allNotes.clear()
                    allNotes.addAll(notes)
                    nursingNotes.clear()
                    nursingNotes.addAll(allNotes)
                    adapter.updateNotes(nursingNotes)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar notas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ✅ Filtrar notas según el texto ingresado en la barra de búsqueda
    private fun filterNotes(query: String) {
        val filteredList = allNotes.filter { note ->
            val titulo = note["titulo"] as? String ?: ""
            val fecha = note["fecha"] as? String ?: ""
            val enfermera = note["enfermera"] as? String ?: ""
            val descripcion = note["descripcion"] as? String ?: ""

            // Filtrar si el query está en el título, fecha, enfermera o descripción
            titulo.contains(query, ignoreCase = true) ||
                    fecha.contains(query, ignoreCase = true) ||
                    enfermera.contains(query, ignoreCase = true) ||
                    descripcion.contains(query, ignoreCase = true)
        }

        nursingNotes.clear()
        nursingNotes.addAll(filteredList)
        adapter.updateNotes(nursingNotes)
    }

    // ✅ Configurar Footer de Navegación
    private fun setupFooterNavigation() {
        try {
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
        } catch (e: Exception) {
            Log.e("FooterError", "Error al inicializar el footer: ${e.message}")
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
