package com.example.vitalage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.model.MedicalControl

class MedicalControlAdapter(private val medications: List<MedicalControl>) :
    RecyclerView.Adapter<MedicalControlAdapter.MedicalControlViewHolder>() {

    class MedicalControlViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMedicationName)
        val tvLot: TextView = view.findViewById(R.id.tvMedicationLot)
        val tvInvima: TextView = view.findViewById(R.id.tvMedicationInvima)
        val tvQuantity: TextView = view.findViewById(R.id.tvMedicationQuantity)
        val tvExpirationDate: TextView = view.findViewById(R.id.tvMedicationExpirationDate)
        val tvStartDate: TextView = view.findViewById(R.id.tvMedicationStartDate)
        val tvEndDate: TextView = view.findViewById(R.id.tvMedicationEndDate)
        val tvObservations: TextView = view.findViewById(R.id.tvMedicationObservations)
        val tvNurse: TextView = view.findViewById(R.id.tvMedicationNurse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalControlViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medical_control, parent, false)
        return MedicalControlViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalControlViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvName.text = medication.name
        holder.tvLot.text = medication.lot
        holder.tvInvima.text = medication.invima
        holder.tvQuantity.text = medication.quantity.toString()
        holder.tvExpirationDate.text = medication.expirationDate
        holder.tvStartDate.text = medication.startDate
        holder.tvEndDate.text = medication.endDate
        holder.tvObservations.text = medication.observations
        holder.tvNurse.text = medication.nurse
    }

    override fun getItemCount(): Int = medications.size
}
