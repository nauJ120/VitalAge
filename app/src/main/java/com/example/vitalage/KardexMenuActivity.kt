package com.example.vitalage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class KardexMenuActivity : AppCompatActivity() {

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kardex_menu)

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
    }
}