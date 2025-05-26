package com.example.vitalage.medico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.generales.InformePacienteActivity
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.generales.IniciarSesionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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
            if (rol != "Medico") {
                Toast.makeText(this, "Solo médicos pueden acceder", Toast.LENGTH_SHORT).show()
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
