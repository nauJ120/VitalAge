package com.example.vitalage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Resident(val name: String, val id: String, val gender: String, val age: Int)

class ResidentAdapter(
    private var residents: List<Resident>,
    private val onEditClick: (Resident) -> Unit,
    private val onDeleteClick: (Resident) -> Unit
) : RecyclerView.Adapter<ResidentAdapter.ResidentViewHolder>() {

    inner class ResidentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.tvResidentName)
        private val detailsText: TextView = view.findViewById(R.id.tvResidentInfo)
        private val idText: TextView = view.findViewById(R.id.tvResidentId)
        private val icon: ImageView = view.findViewById(R.id.imgResident)
        private val btnEdit: ImageButton = view.findViewById(R.id.btnEditResident)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteResident)
        private val genderText: TextView = view.findViewById(R.id.tvResidentGender)

        fun bind(resident: Resident) {
            nameText.text = resident.name
            detailsText.text = "Edad: ${resident.age} años"
            idText.text = "Cédula: ${resident.id}"
            genderText.text = "Sexo: ${resident.gender}"
            icon.setImageResource(R.drawable.ic_resident)

            // Click listeners para editar y eliminar
            btnEdit.setOnClickListener { onEditClick(resident) }
            btnDelete.setOnClickListener { onDeleteClick(resident) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resident, parent, false)
        return ResidentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResidentViewHolder, position: Int) {
        holder.bind(residents[position])
    }

    override fun getItemCount(): Int = residents.size

    fun updateData(newResidents: List<Resident>) {
        residents = newResidents
        notifyDataSetChanged()
    }
}
