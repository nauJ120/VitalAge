package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.clases.Terapia

class TerapiaAdapter(
    private var terapias: List<Terapia>,
    private val onItemClick: (Terapia) -> Unit
) : RecyclerView.Adapter<TerapiaAdapter.TerapiaViewHolder>() {

    inner class TerapiaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tipoTerapiaText: TextView = view.findViewById(R.id.tvTerapiaTipo)
        private val fechaText: TextView = view.findViewById(R.id.tvTerapiaFecha)
        private val encargadoText: TextView = view.findViewById(R.id.tvTerapiaEncargado)
        private val estadoText: TextView = view.findViewById(R.id.tvTerapiaEstado)

        fun bind(terapia: Terapia) {
            tipoTerapiaText.text = terapia.tipo
            fechaText.text = "Fecha: ${terapia.fecha}"
            encargadoText.text = "Encargado: ${terapia.encargado}"
            estadoText.text = if (terapia.realizada) {
                "Estado: Realizada"
            } else {
                "Estado: No Realizada"
            }

            itemView.setOnClickListener { onItemClick(terapia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TerapiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_terapia, parent, false)
        return TerapiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TerapiaViewHolder, position: Int) {
        holder.bind(terapias[position])
    }

    override fun getItemCount(): Int = terapias.size

    fun updateData(newTerapias: List<Terapia>) {
        terapias = newTerapias
        notifyDataSetChanged()
    }
}
