package com.example.vitalage


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.model.MedicationCard

class MedicationCardAdapter(private val medications: List<MedicationCard>) :
    RecyclerView.Adapter<MedicationCardAdapter.MedicationCardViewHolder>() {

    class MedicationCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMedicationName)
        val tvDose: TextView = view.findViewById(R.id.tvMedicationDose)
        val tvObservation: TextView = view.findViewById(R.id.tvMedicationObservation)
        val tvSchedule: TextView = view.findViewById(R.id.tvMedicationSchedule) // 🔥 Nuevo campo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medication_card, parent, false)
        return MedicationCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationCardViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvName.text = medication.name
        holder.tvDose.text = medication.dose
        holder.tvObservation.text = medication.observation

        // 🔥 Concatenar horarios en un solo String con separadores "•"
        holder.tvSchedule.text = "${medication.morning} • ${medication.afternoon} • ${medication.night}"
    }

    override fun getItemCount(): Int = medications.size
}
