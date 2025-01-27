package com.example.vitalage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.databinding.ActivityMedicalControlBinding

class MedicalControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalControlBinding
    private lateinit var medicalControlAdapter: MedicalControlAdapter

    // Lista inicial de medicamentos (simulada)
    private val medicationList = mutableListOf(
        MedicalControl(
            name = "Paracetamol",
            lot = "L12345",
            invima = "INV1234",
            quantity = 20,
            expirationDate = "12/2025",
            startDate = "01/2023",
            endDate = "02/2023",
            observations = "Ninguna",
            nurse = "Auxiliar: Ana López"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        setupRecyclerView()

        // Botón para agregar nuevo medicamento
        binding.btnAddMedication.setOnClickListener {
            showAddMedicationDialog()
        }
    }

    private fun setupRecyclerView() {
        medicalControlAdapter = MedicalControlAdapter(medicationList)
        binding.rvMedicalControl.layoutManager = LinearLayoutManager(this)
        binding.rvMedicalControl.adapter = medicalControlAdapter
    }

    private fun showAddMedicationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medical_control, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Configurar botones del diálogo
        dialogView.findViewById<android.widget.Button>(R.id.btnDialogAdd).setOnClickListener {
            // Obtener datos ingresados por el usuario
            val name = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationName).text.toString()
            val lot = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationLot).text.toString()
            val invima = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationInvima).text.toString()
            val quantity = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationQuantity).text.toString().toIntOrNull()
            val expirationDate = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationExpirationDate).text.toString()
            val startDate = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationStartDate).text.toString()
            val endDate = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationEndDate).text.toString()
            val observations = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationObservations).text.toString()
            val nurse = dialogView.findViewById<android.widget.EditText>(R.id.etMedicationNurse).text.toString()

            // Validar campos obligatorios
            if (name.isNotEmpty() && lot.isNotEmpty() && invima.isNotEmpty() && quantity != null) {
                val newMedication = MedicalControl(
                    name = name,
                    lot = lot,
                    invima = invima,
                    quantity = quantity,
                    expirationDate = expirationDate,
                    startDate = startDate,
                    endDate = endDate,
                    observations = observations,
                    nurse = "Auxiliar: $nurse"
                )
                medicationList.add(newMedication)
                medicalControlAdapter.notifyItemInserted(medicationList.size - 1)
                dialog.dismiss()
                Toast.makeText(this, "Medicamento agregado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<android.widget.Button>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }
    }
}

data class MedicalControl(
    val name: String,
    val lot: String,
    val invima: String,
    val quantity: Int,
    val expirationDate: String,
    val startDate: String,
    val endDate: String,
    val observations: String,
    val nurse: String
)

class MedicalControlAdapter(private val medications: List<MedicalControl>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<MedicalControlAdapter.MedicalControlViewHolder>() {

    class MedicalControlViewHolder(val view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvName: android.widget.TextView = view.findViewById(R.id.tvMedicationName)
        val tvLot: android.widget.TextView = view.findViewById(R.id.tvMedicationLot)
        val tvInvima: android.widget.TextView = view.findViewById(R.id.tvMedicationInvima)
        val tvQuantity: android.widget.TextView = view.findViewById(R.id.tvMedicationQuantity)
        val tvExpirationDate: android.widget.TextView = view.findViewById(R.id.tvMedicationExpirationDate)
        val tvStartDate: android.widget.TextView = view.findViewById(R.id.tvMedicationStartDate)
        val tvEndDate: android.widget.TextView = view.findViewById(R.id.tvMedicationEndDate)
        val tvObservations: android.widget.TextView = view.findViewById(R.id.tvMedicationObservations)
        val tvNurse: android.widget.TextView = view.findViewById(R.id.tvMedicationNurse)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MedicalControlViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_control, parent, false)
        return MedicalControlViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalControlViewHolder, position: Int) {
        val medication = medications[position]
        holder.tvName.text = medication.name
        holder.tvLot.text = medication.lot
        holder.tvInvima.text = medication.invima
        holder.tvQuantity.text = medication.quantity.toString()
        holder.tvExpirationDate.text = medication.expirationDate
        holder.tvStartDate.text = medication.startDate
        holder.tvEndDate.text = medication.endDate
        holder.tvObservations.text = medication.observations
        holder.tvNurse.text = medication.nurse
    }

    override fun getItemCount(): Int = medications.size
}