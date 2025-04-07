package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.R
import com.example.vitalage.model.SignosVitales

class SignosVitalesAdapter(private val listaSignos: List<SignosVitales>) :
    RecyclerView.Adapter<SignosVitalesAdapter.SignoViewHolder>() {

    class SignoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvFrecuenciaCardiaca: TextView = itemView.findViewById(R.id.tvFrecuenciaCardiaca)
        val tvFrecuenciaRespiratoria: TextView = itemView.findViewById(R.id.tvFrecuenciaRespiratoria)
        val tvSaturacionOxigeno: TextView = itemView.findViewById(R.id.tvSaturacionOxigeno)
        val tvPresionArterial: TextView = itemView.findViewById(R.id.tvPresionArterial)
        val tvTemperatura: TextView = itemView.findViewById(R.id.tvTemperatura)
        val tvPeso: TextView = itemView.findViewById(R.id.tvPeso)
        val tvImc: TextView = itemView.findViewById(R.id.tvImc)
        val tvEncargado: TextView = itemView.findViewById(R.id.tvEncargado)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signos_vitales, parent, false)
        return SignoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SignoViewHolder, position: Int) {
        val signo = listaSignos[position]

        holder.tvFecha.text = "📅 ${signo.fecha}"
        holder.tvFrecuenciaCardiaca.text = "❤️ Frecuencia Cardíaca: ${signo.frecuenciaCardiaca} bpm"
        holder.tvFrecuenciaRespiratoria.text = "💨 Frecuencia Respiratoria: ${signo.frecuenciaRespiratoria} rpm"
        holder.tvSaturacionOxigeno.text = "🫁 Saturación de Oxígeno: ${signo.saturacionOxigeno}%"
        holder.tvPresionArterial.text = "🩸 Presión Arterial: ${signo.presionArterial}"
        holder.tvTemperatura.text = "🌡️ Temperatura: ${signo.temperatura} °C"
        holder.tvPeso.text = "⚖️ Peso: ${signo.peso} kg"
        holder.tvImc.text = "📊 IMC: ${signo.imc}"
        holder.tvEncargado.text = "😷 Encargada: ${signo.encargado}"
    }



    override fun getItemCount(): Int = listaSignos.size
}
