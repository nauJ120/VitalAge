package com.example.vitalage

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.MedicationAdapter
import com.example.vitalage.databinding.ActivityInventoryBinding
import com.example.vitalage.model.Medication
import com.google.firebase.firestore.FirebaseFirestore

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var medicationAdapter: MedicationAdapter
    private val medicationList = mutableListOf<Medication>()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del Intent
        val patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        val patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        val patientAge = intent.getIntExtra("patient_age", 0)

        // Mostrar datos del paciente en la UI
        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar medicamentos desde Firestore
        fetchMedicationsFromFirestore()
    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationAdapter(medicationList) { medication ->
            showAdministerDialog(medication)
        }
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter
    }

    private fun fetchMedicationsFromFirestore() {
        val patientRef = firestore.collection("Pacientes").document(patientId)

        patientRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 🔥 Extraer el array de medicamentos como una lista de mapas
                    val medications = document.get("medicamentos") as? List<Map<String, Any>> ?: emptyList()

                    medicationList.clear()

                    for (medication in medications) {
                        val name = medication["nombre"] as? String ?: "Desconocido"
                        val cantidad = (medication["cantidad"] as? Long)?.toInt() ?: 0
                        val dosis = (medication["dosis"] as? Long)?.toInt() ?: 0
                        val auxiliar = medication["auxiliar"] as? String ?: "N/A"
                        val fechaInicio = medication["fecha_inicio"] as? String ?: "-"
                        val fechaFin = medication["fecha_fin"] as? String ?: "-"
                        val fechaVencimiento = medication["fecha_vencimiento"] as? String ?: "-"
                        val horaMañana = medication["hora_mañana"] as? String ?: "-"
                        val horaTarde = medication["hora_tarde"] as? String ?: "-"
                        val horaNoche = medication["hora_noche"] as? String ?: "-"
                        val invima = medication["invima"] as? String ?: "-"
                        val lote = medication["lote"] as? String ?: "-"
                        val observaciones = medication["observaciones"] as? String ?: "-"

                        // Crear objeto Medication y agregarlo a la lista
                        medicationList.add(
                            Medication(
                                nombre = name,
                                cantidad = cantidad,
                                dosis = dosis,
                                enfermero = auxiliar,
                                fecha_inicio = fechaInicio,
                                fecha_fin = fechaFin,
                                fecha_vencimiento = fechaVencimiento,
                                hora_mañana = horaMañana,
                                hora_tarde = horaTarde,
                                hora_noche = horaNoche,
                                invima = invima,
                                lote = lote,
                                observaciones = observaciones
                            )
                        )
                    }

                    medicationAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No se encontraron medicamentos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener medicamentos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showAdministerDialog(medication: Medication) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_administer_medication, null)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etAdministerQuantity)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Administrar ${medication.nombre}")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val administeredQuantity = etQuantity.text.toString().trim().toIntOrNull()

                if (administeredQuantity == null || administeredQuantity <= 0) {
                    Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
                } else if (administeredQuantity > medication.cantidad) {
                    Toast.makeText(this, "No hay suficiente cantidad disponible", Toast.LENGTH_SHORT).show()
                } else {
                    updateMedicationQuantity(medication, administeredQuantity)
                }
            }
            .setNegativeButton("Cancelar", null)

        dialogBuilder.create().show()
    }


    private fun updateMedicationQuantity(medication: Medication, administeredQuantity: Int) {
        val patientRef = firestore.collection("Pacientes").document(patientId)

        patientRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtener la lista actual de medicamentos
                    val medications = document.get("medicamentos") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Buscar el medicamento a actualizar
                    val updatedMedications = medications.map { med ->
                        if (med["nombre"] == medication.nombre) {
                            med.toMutableMap().apply {
                                val currentQuantity = (this["cantidad"] as? Long)?.toInt() ?: 0
                                this["cantidad"] = currentQuantity - administeredQuantity
                            }
                        } else {
                            med
                        }
                    }

                    // Actualizar la lista de medicamentos en Firestore
                    patientRef.update("medicamentos", updatedMedications)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Medicamento administrado correctamente", Toast.LENGTH_SHORT).show()

                            // Actualizar en la lista local
                            medicationList.find { it.nombre == medication.nombre }?.cantidad =
                                (medication.cantidad - administeredQuantity)
                            medicationAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al actualizar cantidad: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener datos del paciente: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
