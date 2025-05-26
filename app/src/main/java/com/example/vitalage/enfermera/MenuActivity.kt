package com.example.vitalage.enfermera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.generales.InformePacienteActivity
import com.example.vitalage.adapters.MenuAdapter
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.clases.MenuOption
import com.example.vitalage.generales.IniciarSesionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            val tvUser = findViewById<TextView>(R.id.tvNombreUsuario)
            tvUser.text = "Enfermera: $usuarioActual"
        }

        // Botón de retroceso
        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
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
                    intent.putExtra("patient_id", patientId)
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

    private fun insertarSignosVitalesPrueba(patientId: String) {
        val db = FirebaseFirestore.getInstance()
        val signosVitales = mutableListOf<Map<String, Any>>()

        // FORMATO DE FECHA
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // === REGISTROS PARA HOY (1 día) ===
        signosVitales.add(mapOf(
            "fecha" to LocalDateTime.now().withHour(6).withMinute(0).format(formatter),
            "frecuencia_cardiaca" to 65, "frecuencia_respiratoria" to 20,
            "saturacion_oxigeno" to 96, "presion_arterial" to 110,
            "temperatura" to 36.5, "peso" to 60.0, "imc" to 21.5,
            "escala_dolor" to 2, "encargado" to "Simulación 1 Día - AM"
        ))

        signosVitales.add(mapOf(
            "fecha" to LocalDateTime.now().withHour(14).withMinute(30).format(formatter),
            "frecuencia_cardiaca" to 70, "frecuencia_respiratoria" to 22,
            "saturacion_oxigeno" to 95, "presion_arterial" to 115,
            "temperatura" to 37.1, "peso" to 60.5, "imc" to 21.8,
            "escala_dolor" to 4, "encargado" to "Simulación 1 Día - PM"
        ))

        signosVitales.add(mapOf(
            "fecha" to LocalDateTime.now().withHour(20).withMinute(45).format(formatter),
            "frecuencia_cardiaca" to 68, "frecuencia_respiratoria" to 19,
            "saturacion_oxigeno" to 97, "presion_arterial" to 118,
            "temperatura" to 36.9, "peso" to 60.2, "imc" to 21.6,
            "escala_dolor" to 3, "encargado" to "Simulación 1 Día - Noche"
        ))

        // === REGISTROS PARA 5 días atrás ===
        for (i in 1..3) {
            val fecha = LocalDateTime.now().minusDays(5 - i.toLong()).withHour(10 + i)
            signosVitales.add(mapOf(
                "fecha" to fecha.format(formatter),
                "frecuencia_cardiaca" to (60 + i), "frecuencia_respiratoria" to (18 + i),
                "saturacion_oxigeno" to (94 + i), "presion_arterial" to (108 + i),
                "temperatura" to (36 + i * 0.2), "peso" to (60 + i), "imc" to (20 + i),
                "escala_dolor" to i, "encargado" to "Simulación 5 Días #$i"
            ))
        }

        // === REGISTROS PARA 1 mes atrás ===
        for (i in 1..3) {
            val fecha = LocalDateTime.now().minusDays(10 * i.toLong()).withHour(9 + i)
            signosVitales.add(mapOf(
                "fecha" to fecha.format(formatter),
                "frecuencia_cardiaca" to (72 + i), "frecuencia_respiratoria" to (21 + i),
                "saturacion_oxigeno" to (93 + i), "presion_arterial" to (110 + i * 2),
                "temperatura" to (36.3 + i * 0.3), "peso" to (62 + i), "imc" to (22 + i),
                "escala_dolor" to (i + 1), "encargado" to "Simulación 1 Mes #$i"
            ))
        }

        // === REGISTROS PARA 1 año atrás ===
        for (i in 1..3) {
            val fecha = LocalDateTime.now().minusMonths(4 * i.toLong()).withDayOfMonth(10 + i).withHour(10)
            signosVitales.add(mapOf(
                "fecha" to fecha.format(formatter),
                "frecuencia_cardiaca" to (75 + i), "frecuencia_respiratoria" to (20 + i),
                "saturacion_oxigeno" to (92 + i), "presion_arterial" to (120 + i * 2),
                "temperatura" to (36.7 + i * 0.2), "peso" to (63 + i), "imc" to (22 + i * 0.5),
                "escala_dolor" to (i + 2), "encargado" to "Simulación 1 Año #$i"
            ))
        }

        // Actualizar Firestore
        db.collection("Pacientes").document(patientId)
            .update("signos_vitales", signosVitales)
            .addOnSuccessListener {
                Log.d("FirestoreTest", "Datos insertados correctamente")
            }
            .addOnFailureListener {
                Log.e("FirestoreTest", "Error insertando datos: ${it.message}")
            }
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
