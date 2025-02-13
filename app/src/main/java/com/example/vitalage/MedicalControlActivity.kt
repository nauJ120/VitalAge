package com.example.vitalage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
            nurse = "Auxiliar: Ana López",
            morningTime = "08:00 AM",
            afternoonTime = "02:00 PM",
            nightTime = "08:00 PM",
            dosis = 1
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

        // Referencias a los campos de texto
        val etDosis = dialogView.findViewById<EditText>(R.id.etMedicationDosis)
        val etName = dialogView.findViewById<EditText>(R.id.etMedicationName)
        val etLot = dialogView.findViewById<EditText>(R.id.etMedicationLot)
        val etInvima = dialogView.findViewById<EditText>(R.id.etMedicationInvima)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etMedicationQuantity)
        val etExpirationDate = dialogView.findViewById<EditText>(R.id.etMedicationExpirationDate)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etMedicationStartDate)
        val etEndDate = dialogView.findViewById<EditText>(R.id.etMedicationEndDate)
        val etObservations = dialogView.findViewById<EditText>(R.id.etMedicationObservations)
        val etNurse = dialogView.findViewById<EditText>(R.id.etMedicationNurse)
        val etMorningTime = dialogView.findViewById<EditText>(R.id.etMedicationMorningTime)
        val etAfternoonTime = dialogView.findViewById<EditText>(R.id.etMedicationAfternoonTime)
        val etNightTime = dialogView.findViewById<EditText>(R.id.etMedicationNightTime)

        // Botón para agregar el medicamento
        dialogView.findViewById<Button>(R.id.btnDialogAdd).setOnClickListener {
            val name = etName.text.toString().trim()
            val lot = etLot.text.toString().trim()
            val invima = etInvima.text.toString().trim()
            val quantity = etQuantity.text.toString().trim().toIntOrNull()
            val expirationDate = etExpirationDate.text.toString().trim()
            val startDate = etStartDate.text.toString().trim()
            val endDate = etEndDate.text.toString().trim()
            val observations = etObservations.text.toString().trim()
            val nurse = etNurse.text.toString().trim()
            val dosis = etDosis.text.toString().trim().toIntOrNull()

            // Obtener las horas de administración
            val morningTime = etMorningTime.text.toString().trim()
            val afternoonTime = etAfternoonTime.text.toString().trim()
            val nightTime = etNightTime.text.toString().trim()

            // Validación de campos obligatorios
            if (name.isEmpty() || lot.isEmpty() || invima.isEmpty() || quantity == null || dosis == null ||
                expirationDate.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || nurse.isEmpty() ||
                morningTime.isEmpty() || afternoonTime.isEmpty() || nightTime.isEmpty()) {

                Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                val newMedication = MedicalControl(
                    name = name,
                    lot = lot,
                    invima = invima,
                    quantity = quantity,
                    expirationDate = expirationDate,
                    startDate = startDate,
                    endDate = endDate,
                    observations = observations,
                    nurse = "Auxiliar: $nurse",
                    morningTime = morningTime,
                    afternoonTime = afternoonTime,
                    nightTime = nightTime,
                    dosis = dosis
                )

                medicationList.add(newMedication)
                medicalControlAdapter.notifyItemInserted(medicationList.size - 1)
                dialog.dismiss()
                Toast.makeText(this, "Medicamento agregado correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para cancelar
        dialogView.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
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
    val nurse: String,
    val morningTime: String,
    val afternoonTime: String,
    val nightTime: String,
    val dosis: Int
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