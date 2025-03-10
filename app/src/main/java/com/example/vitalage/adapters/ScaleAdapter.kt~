package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.clases.Escala

class ScaleAdapter(private val escalaList: List<Escala>) :
    RecyclerView.Adapter<ScaleAdapter.ScaleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scale, parent, false)
        return ScaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScaleViewHolder, position: Int) {
        val escala = escalaList[position]
        holder.bind(escala)
    }

    override fun getItemCount(): Int = escalaList.size

    class ScaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tv_scale_type)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_scale_date)
        private val tvScore: TextView = itemView.findViewById(R.id.tv_scale_score)
        private val tvEncargado: TextView = itemView.findViewById(R.id.tv_scale_encargado)

        fun bind(escala: Escala) {
            tvType.text = escala.tipo
            tvDate.text = escala.fecha
            tvScore.text = "Puntaje: ${escala.puntaje}"
            tvEncargado.text = "Encargado: ${escala.encargado}"
        }
    }
}
