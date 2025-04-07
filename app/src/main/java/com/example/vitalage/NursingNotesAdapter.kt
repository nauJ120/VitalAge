package com.example.vitalage

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NursingNotesAdapter(
    private var notes: List<Map<String, Any>>, // Lista de notas
    private val patientId: String, // ID del paciente en Firestore
    private val onNoteDeleted: () -> Unit // Callback para actualizar la lista
) : RecyclerView.Adapter<NursingNotesAdapter.NursingNoteViewHolder>() {

    inner class NursingNoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val noteText: TextView = view.findViewById(R.id.tv_note)
        private val btnDelete: ImageView = view.findViewById(R.id.btn_delete)

        fun bind(note: Map<String, Any>) {
            val titulo = note["titulo"] as? String ?: "Sin título"
            val fecha = note["fecha"] as? String ?: "Fecha desconocida"
            val enfermera = note["enfermera"] as? String ?: "Enfermera desconocida"
            val descripcion = note["descripcion"] as? String ?: "Sin descripción"

            noteText.text = "📌 $titulo\n$fecha - $enfermera: $descripcion"

            // ✅ Botón para eliminar nota
            btnDelete.setOnClickListener {
                showDeleteConfirmationDialog(note)
            }
        }

        private fun showDeleteConfirmationDialog(note: Map<String, Any>) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Eliminar Nota")
                .setMessage("¿Estás seguro de que deseas eliminar esta nota?")
                .setPositiveButton("Eliminar") { _, _ -> deleteNoteFromFirestore(note) }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        private fun deleteNoteFromFirestore(note: Map<String, Any>) {
            val db = FirebaseFirestore.getInstance()
            val patientRef = db.collection("Pacientes").document(patientId)

            patientRef.update("notasEnfermeria", FieldValue.arrayRemove(note))
                .addOnSuccessListener {
                    Toast.makeText(itemView.context, "Nota eliminada", Toast.LENGTH_SHORT).show()
                    onNoteDeleted() // ✅ Llamar la función para actualizar la lista
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al eliminar la nota: ${e.message}")
                    Toast.makeText(itemView.context, "Error al eliminar la nota", Toast.LENGTH_SHORT).show()
                }
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

    fun updateNotes(newNotes: List<Map<String, Any>>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
