package com.example.vitalage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.databinding.ActivityInventoryBinding


class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var medicationAdapter: MedicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lista de ejemplo
        val medications = listOf(
            Medication("Paracetamol", 20),
            Medication("Ibuprofeno", 15),
            Medication("Amoxicilina", 10)
        )

        // Configurar RecyclerView
        medicationAdapter = MedicationAdapter(medications) { medication ->
            showAdministerDialog(medication)
        }
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter

    }

    private fun showAdministerDialog(medication: Medication) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Administrar Medicamento")
        builder.setMessage("¿Cuántos ${medication.name} desea administrar?")
        builder.setPositiveButton("Confirmar") { _, _ ->
            Toast.makeText(this, "${medication.name} administrado", Toast.LENGTH_SHORT).show()
            // Aquí podrías actualizar el inventario
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}

data class Medication(val name: String, var quantity: Int)

class MedicationAdapter(
    private val medications: List<Medication>,
    private val onAdministerClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvMedicationName: android.widget.TextView = view.findViewById(R.id.tvMedicationName)
        val tvMedicationQuantity: android.widget.TextView = view.findViewById(R.id.tvMedicationQuantity)
        val btnAdminister: android.widget.Button = view.findViewById(R.id.btnAdminister)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MedicationViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvMedicationName.text = medication.name
        holder.tvMedicationQuantity.text = medication.quantity.toString()
        holder.btnAdminister.setOnClickListener { onAdministerClick(medication) }
    }

    override fun getItemCount(): Int = medications.size
}