package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R

data class Patient(val name: String, val id: String, val gender: String, val age: Int)

class PatientAdapter(
    private var patients: List<Patient>,
    private val onItemClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.tv_patient_name)
        private val detailsText: TextView = view.findViewById(R.id.tv_patient_details)
        private val icon: ImageView = view.findViewById(R.id.iv_patient_icon)

        fun bind(patient: Patient) {
            nameText.text = patient.name
            detailsText.text = "ID: ${patient.id} | Sexo: ${patient.gender} | Edad: ${patient.age}"
            icon.setImageResource(R.drawable.ic_user)
            itemView.setOnClickListener { onItemClick(patient) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount(): Int = patients.size

    fun updateData(newPatients: List<Patient>) {
        patients = newPatients
        notifyDataSetChanged()
    }

}
