package com.example.vitalage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NursingNotesAdapter(private var notes: List<String>) :
    RecyclerView.Adapter<NursingNotesAdapter.NursingNoteViewHolder>() {

    inner class NursingNoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val noteText: TextView = view.findViewById(R.id.tv_note)

        fun bind(note: String) {
            noteText.text = note
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NursingNoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nursing_note, parent, false)
        return NursingNoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NursingNoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<String>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
