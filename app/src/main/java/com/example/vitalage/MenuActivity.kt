package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MenuActivity : AppCompatActivity() {

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Botón de retroceso
        val backButton = findViewById<ImageView>(R.id.iv_back)
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


                "Cámara" -> {
                    val intent = Intent(this, CamaraActivity::class.java)
                    startActivity(intent)
                }

                "Signos vitales" -> {
                    // Redirigir a la actividad de "Signos vitales"
                    val intent = Intent(this, SignosVitalesActivity::class.java)
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
}
