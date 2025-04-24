package com.example.vitalage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.model.Medication

class MedicationAdapter(
    private var medications: MutableList<Medication>,
    private val onAdministerClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMedicationName: TextView = view.findViewById(R.id.tvMedicationName)
        val tvMedicationQuantity: TextView = view.findViewById(R.id.tvMedicationQuantity)
        val btnAdminister: Button = view.findViewById(R.id.btnAdminister)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvMedicationName.text = medication.nombre
        holder.tvMedicationQuantity.text = medication.cantidad.toString()
        holder.btnAdminister.setOnClickListener { onAdministerClick(medication) }
    }

    override fun getItemCount(): Int = medications.size

    fun updateData(newList: List<Medication>) {
        medications = newList.toMutableList() // 🔥 Crear una nueva lista mutable
        notifyDataSetChanged()
    }

}
