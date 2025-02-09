package com.example.vitalage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.models.SignoVital
import com.example.vitalage.R

class SignosVitalesAdapter(private var lista: List<SignoVital>) :
    RecyclerView.Adapter<SignosVitalesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fecha: TextView = view.findViewById(R.id.tvFecha)
        val presion: TextView = view.findViewById(R.id.tvPresion)
        val frecuencia: TextView = view.findViewById(R.id.tvFrecuencia)
        val oxigeno: TextView = view.findViewById(R.id.tvOxigeno)
        val temperatura: TextView = view.findViewById(R.id.tvTemperatura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_signo_vital, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val signo = lista[position]
        holder.fecha.text = "Fecha: ${signo.fecha}"
        holder.presion.text = "Presión Arterial: ${signo.presionArterial} mmHg"
        holder.frecuencia.text = "Frecuencia Cardíaca: ${signo.frecuenciaCardiaca} BPM"
        holder.oxigeno.text = "Saturación de Oxígeno: ${signo.oxigenacion}%"
        holder.temperatura.text = "Temperatura: ${signo.temperatura}°C"
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<SignoVital>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
