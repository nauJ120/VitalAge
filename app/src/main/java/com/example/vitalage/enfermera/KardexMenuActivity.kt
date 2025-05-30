package com.example.vitalage.enfermera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.databinding.ActivityKardexMenuBinding
import com.example.vitalage.generales.IniciarSesionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KardexMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKardexMenuBinding
    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String

    private var patientAge: Int = 0

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kardex_menu)

        // Obtener nombre de usuario actual
        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.tvUser)
            tvUser.text = "Enfermera: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Obtener referencias de los TextView
        val tvResidentName = findViewById<TextView>(R.id.tvResidentName)
        val tvResidentInfo = findViewById<TextView>(R.id.tvResidentInfo)

        // Modificar los TextView con los datos del paciente
        tvResidentName.text = patientName
        tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Referencias a los botones
        val btnInventory = findViewById<Button>(R.id.btnInventory)
        val btnMedCard = findViewById<Button>(R.id.btnMedCard)
        val btnControl = findViewById<Button>(R.id.btnControl)
        val btnHistory = findViewById<Button>(R.id.btnDoseHistory)

        val btnHomeContainer = findViewById<LinearLayout>(R.id.btnHomeContainer)

        btnHomeContainer.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java) // Reemplaza "NuevaActividad" con el nombre de tu actividad destino
            startActivity(intent)
        }

        btnInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_gender", patientGender)
            intent.putExtra("patient_age", patientAge)
            startActivity(intent)
        }

        btnMedCard.setOnClickListener {
            val intent = Intent(this, MedicationCardActivity::class.java)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_gender", patientGender)
            intent.putExtra("patient_age", patientAge)
            startActivity(intent)
        }

        btnControl.setOnClickListener {
            val intent = Intent(this, MedicalControlActivity::class.java)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_gender", patientGender)
            intent.putExtra("patient_age", patientAge)
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, DoseHistoryActivity::class.java)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_gender", patientGender)
            intent.putExtra("patient_age", patientAge)
            startActivity(intent)
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

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            // Usuario no logeado
            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        val userId = user.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("user/users/$userId")

        dbRef.get().addOnSuccessListener { snapshot ->
            val rol = snapshot.child("rol").value.toString()

            // Aquí comparas según el rol esperado
            if (rol != "Enfermera") {
                Toast.makeText(this, "Acceso solo para enfermeras", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar el rol", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
        }
    }
}