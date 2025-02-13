package com.example.vitalage

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.databinding.ActivityMedicationCardBinding
import com.google.firebase.firestore.FirebaseFirestore

class MedicationCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationCardBinding
    private lateinit var medicationAdapter: MedicationCardAdapter
    private val medicationList = mutableListOf<MedicationCard>()

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del Intent
        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Mostrar datos del paciente
        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Configurar RecyclerView
        setupRecyclerView()

        // Obtener los medicamentos del paciente desde Firestore
        fetchMedicationsFromFirestore()
    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationCardAdapter(medicationList)
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter
    }

    private fun fetchMedicationsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val patientRef = db.collection("Pacientes").document(patientId)

        patientRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val medications = document.get("medicamentos") as? List<Map<String, Any>> ?: emptyList()

                medicationList.clear()

                for (medication in medications) {
                    val name = medication["nombre"] as? String ?: "Desconocido"
                    val dose = "${medication["dosis"]?.toString() ?: "N/A"} mg"
                    val observation = medication["observaciones"] as? String ?: "N/A"
                    val morning = medication["hora_mañana"] as? String ?: "-"
                    val afternoon = medication["hora_tarde"] as? String ?: "-"
                    val night = medication["hora_noche"] as? String ?: "-"

                    medicationList.add(MedicationCard(name, dose, observation, morning, afternoon, night))
                }

                // Notificar cambios en la lista
                medicationAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No se encontraron medicamentos para este paciente", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al obtener medicamentos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


data class MedicationCard(
    val name: String,
    val dose: String,
    val observation: String,
    val morning: String,
    val afternoon: String,
    val night: String
)

class MedicationCardAdapter(private val medications: List<MedicationCard>) :
    RecyclerView.Adapter<MedicationCardAdapter.MedicationCardViewHolder>() {

    class MedicationCardViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvName: android.widget.TextView = view.findViewById(R.id.tvMedicationName)
        val tvDose: android.widget.TextView = view.findViewById(R.id.tvMedicationDose)
        val tvObservation: android.widget.TextView = view.findViewById(R.id.tvMedicationObservation)
        val tvMorning: android.widget.TextView = view.findViewById(R.id.tvMedicationMorning)
        val tvAfternoon: android.widget.TextView = view.findViewById(R.id.tvMedicationAfternoon)
        val tvNight: android.widget.TextView = view.findViewById(R.id.tvMedicationNight)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MedicationCardViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication_card, parent, false)
        return MedicationCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationCardViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvName.text = medication.name
        holder.tvDose.text = medication.dose
        holder.tvObservation.text = medication.observation
        holder.tvMorning.text = medication.morning
        holder.tvAfternoon.text = medication.afternoon
        holder.tvNight.text = medication.night
    }

    override fun getItemCount(): Int = medications.size
}