package com.example.vitalage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.databinding.ActivityMedicationCardBinding

class MedicationCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationCardBinding
    private lateinit var medicationAdapter: MedicationCardAdapter

    // Lista inicial de medicamentos (simulada)
    private val medicationList = mutableListOf(
        MedicationCard("Paracetamol", "500mg", "Oral", "Cada 8 horas", "8:00 AM", "2:00 PM", "10:00 PM"),
        MedicationCard("Ibuprofeno", "200mg", "Oral", "Cada 12 horas", "9:00 AM", "-", "9:00 PM")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationCardAdapter(medicationList)
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter
    }

}

data class MedicationCard(
    val name: String,
    val dose: String,
    val via: String,
    val frequency: String,
    val morning: String,
    val afternoon: String,
    val night: String
)

class MedicationCardAdapter(private val medications: List<MedicationCard>) :
    RecyclerView.Adapter<MedicationCardAdapter.MedicationCardViewHolder>() {

    class MedicationCardViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvName: android.widget.TextView = view.findViewById(R.id.tvMedicationName)
        val tvDose: android.widget.TextView = view.findViewById(R.id.tvMedicationDose)
        val tvVia: android.widget.TextView = view.findViewById(R.id.tvMedicationVia)
        val tvFrequency: android.widget.TextView = view.findViewById(R.id.tvMedicationFrequency)
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
        holder.tvVia.text = medication.via
        holder.tvFrequency.text = medication.frequency
        holder.tvMorning.text = medication.morning
        holder.tvAfternoon.text = medication.afternoon
        holder.tvNight.text = medication.night
    }

    override fun getItemCount(): Int = medications.size
}