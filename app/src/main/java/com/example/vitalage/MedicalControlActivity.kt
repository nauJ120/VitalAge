package com.example.vitalage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.MedicalControlAdapter
import com.example.vitalage.databinding.ActivityMedicalControlBinding
import com.example.vitalage.model.MedicalControl
import com.google.firebase.database.*

class MedicalControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalControlBinding
    private lateinit var medicalControlAdapter: MedicalControlAdapter
    private val medicationList = mutableListOf<MedicalControl>()

    private lateinit var database: DatabaseReference
    private lateinit var patientId: String // Se obtiene del intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID del paciente desde el Intent
        patientId = intent.getStringExtra("patient_id") ?: return

        // Inicializar Firebase Database
        database = FirebaseDatabase.getInstance().getReference("Pacientes").child(patientId).child("medicamentos")

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar datos desde Firebase
        fetchMedicationsFromDatabase()
    }

    private fun setupRecyclerView() {
        medicalControlAdapter = MedicalControlAdapter(medicationList)
        binding.rvMedicalControl.layoutManager = LinearLayoutManager(this)
        binding.rvMedicalControl.adapter = medicalControlAdapter
    }

    private fun fetchMedicationsFromDatabase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicationList.clear()

                for (medicationSnapshot in snapshot.children) {
                    val medication = medicationSnapshot.getValue(MedicalControl::class.java)
                    medication?.let { medicationList.add(it) }
                }

                medicalControlAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MedicalControlActivity, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
