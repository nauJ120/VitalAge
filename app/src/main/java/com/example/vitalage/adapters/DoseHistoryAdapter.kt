package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.databinding.ItemDoseHistoryBinding
import com.example.vitalage.model.DoseHistory
import java.text.SimpleDateFormat
import java.util.*

class DoseHistoryAdapter(private val doseHistoryList: List<DoseHistory>) :
    RecyclerView.Adapter<DoseHistoryAdapter.DoseHistoryViewHolder>() {

    class DoseHistoryViewHolder(val binding: ItemDoseHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseHistoryViewHolder {
        val binding = ItemDoseHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoseHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoseHistoryViewHolder, position: Int) {
        val dose = doseHistoryList[position]
        holder.binding.tvMedicationName.text = dose.medicamento
        holder.binding.tvQuantity.text = "Cantidad: ${dose.cantidad}"
        holder.binding.tvDosis.text = "Dosis: ${dose.dosis} mg"
        holder.binding.tvObservations.text = "Observaciones: ${dose.observaciones}"
        holder.binding.tvUser.text = "Administrado por: ${dose.usuario}"

        // Convertir Timestamp a fecha legible
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.binding.tvDate.text = "Fecha: ${dose.fecha_hora?.toDate()?.let { dateFormat.format(it) } ?: "Desconocida"}"
    }

    override fun getItemCount(): Int = doseHistoryList.size
}
