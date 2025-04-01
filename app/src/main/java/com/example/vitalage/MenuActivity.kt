package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuActivity : AppCompatActivity() {

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Obtener nombre de usuario actual
        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.tvUser)
            tvUser.text = "Enfermera: $usuarioActual"
        }
        // Botón de retroceso
        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            finish() // Cierra esta actividad y regresa a la anterior
        }

        // Opciones del menú
        val menuOptions = listOf(
            MenuOption("Notas de enfermería", R.drawable.ic_nursing_notes),
            MenuOption("Signos vitales", R.drawable.ic_vital_signs),
            MenuOption("Escalas", R.drawable.ic_scales),
            MenuOption("Inventario y Control", R.drawable.ic_inventory),
            MenuOption("Terapias", R.drawable.ic_therapies),
            MenuOption("Informes", R.drawable.ic_reports),
            MenuOption("Cámara", R.drawable.ic_camera)
        )

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        // Configurar el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_menu_options)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MenuAdapter(menuOptions) { menuOption ->
            when (menuOption.title) {
                "Notas de enfermería" -> {
                    // Redirigir a la actividad de "Notas de enfermería"
                    val intent = Intent(this, NursingNotesActivity::class.java)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_gender", patientGender)
                    intent.putExtra("patient_age", patientAge)
                    startActivity(intent)
                }

                "Informes" -> {
                    // Redirigir a la actividad de "Informes"
                    val intent = Intent(this, InformePacienteActivity::class.java)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    startActivity(intent)
                }

                "Terapias" -> {
                    // Redirigir a la actividad de "Terapias"
                    val intent = Intent(this, TerapiasActivity::class.java)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_gender", patientGender)
                    intent.putExtra("patient_age", patientAge)
                    startActivity(intent)
                }

                "Cámara" -> {
                    val intent = Intent(this, CamaraActivity::class.java)
                    startActivity(intent)
                }

                "Signos vitales" -> {
                    // Redirigir a la actividad de "Signos vitales"
                    val intent = Intent(this, SignosVitalesActivity::class.java)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_gender", patientGender)
                    intent.putExtra("patient_age", patientAge)
                    startActivity(intent)
                }

                "Inventario y Control" -> {
                    // Redirigir a la actividad de "Inventario y Control"
                    val intent = Intent(this, KardexMenuActivity::class.java)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_gender", patientGender)
                    intent.putExtra("patient_age", patientAge)
                    startActivity(intent)
                }

                "Escalas" -> {
                    // Redirigir a la actividad de "Escalas"
                    val intent = Intent(this, ListaEscalasActivity::class.java)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_gender", patientGender)
                    intent.putExtra("patient_age", patientAge)
                    startActivity(intent)
                }

                else -> {
                    // Acciones para otras opciones
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
