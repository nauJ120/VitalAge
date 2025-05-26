package com.example.vitalage.medico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.generales.InformePacienteActivity
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R

class DoctorPatientDetailActivity : AppCompatActivity() {

    private lateinit var patientId: String
    private lateinit var patientName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_patient_detail)

        // Recibir datos del paciente desde el intent
        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: "Paciente"

        // Mostrar nombre del paciente
        findViewById<TextView>(R.id.tvNombrePaciente).text = "Paciente: $patientName"

        // Botón para ver informe clínico
        findViewById<Button>(R.id.btnVerInforme).setOnClickListener {
            val intent = Intent(this, InformePacienteActivity::class.java).apply {
                putExtra("patient_id", patientId)
                putExtra("patient_name", patientName)
            }
            startActivity(intent)
        }

        // Botón para registrar nueva nota médica
        findViewById<Button>(R.id.btnRegistrarConsulta).setOnClickListener {
            val intent = Intent(this, RegistroNotaMedicaActivity::class.java).apply {
                putExtra("patient_id", patientId)
                putExtra("patient_name", patientName)
            }
            startActivity(intent)
        }

        // Navegación inferior
        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, DoctorPatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Botón de regresar (flecha)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la anterior
        }
    }
}
