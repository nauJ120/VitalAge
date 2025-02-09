package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.models.Medicamento
import com.example.vitalage.R

class MedicamentosAdapter(private var lista: List<Medicamento>) :
    RecyclerView.Adapter<MedicamentosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvMedicationName)
        val lote: TextView = view.findViewById(R.id.tvMedicationLot)
        val invima: TextView = view.findViewById(R.id.tvMedicationInvima)
        val cantidad: TextView = view.findViewById(R.id.tvMedicationQuantity)
        val fechaVencimiento: TextView = view.findViewById(R.id.tvMedicationExpirationDate)
        val fechaInicio: TextView = view.findViewById(R.id.tvMedicationStartDate)
        val fechaFinalizacion: TextView = view.findViewById(R.id.tvMedicationEndDate)
        val observaciones: TextView = view.findViewById(R.id.tvMedicationObservations)
        val auxiliar: TextView = view.findViewById(R.id.tvMedicationNurse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_control, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicamento = lista[position]
        holder.nombre.text = medicamento.nombre
        holder.lote.text = medicamento.lote
        holder.invima.text = medicamento.invima
        holder.cantidad.text = medicamento.cantidad.toString()
        holder.fechaVencimiento.text = medicamento.fechaVencimiento
        holder.fechaInicio.text = medicamento.fechaInicio
        holder.fechaFinalizacion.text = medicamento.fechaFinalizacion
        holder.observaciones.text = medicamento.observaciones
        holder.auxiliar.text = medicamento.auxiliar
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Medicamento>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
