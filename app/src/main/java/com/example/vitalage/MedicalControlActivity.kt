package com.example.vitalage

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.databinding.ActivityMedicalControlBinding
import com.example.vitalage.model.MedicalControl
import com.example.vitalage.model.MedicationCard
import com.example.vitalage.model.SpaceItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MedicalControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalControlBinding
    private lateinit var medicalControlAdapter: MedicalControlAdapter

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    private var usuarioActual: String = "Desconocido"

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

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Usuario: $usuarioActual"
        }

        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        // Mostrar datos del paciente
        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        // Configurar RecyclerView
        setupRecyclerView()

        // Botón para agregar nuevo medicamento
        binding.btnAddMedication.setOnClickListener {
            showAddMedicationDialog()
        }

        binding.rvMedicalControl.addItemDecoration(SpaceItemDecoration(16)) // 16dp de espacio entre elementos


        // Obtener los medicamentos del paciente desde Firestore
        fetchMedicationsFromFirestore()

        val fromScan = intent.getBooleanExtra("from_scan", false)
        if (fromScan) {
            showAddMedicationDialog()
        }
    }

    private fun setupRecyclerView() {
        medicalControlAdapter = MedicalControlAdapter(medicationList)
        binding.rvMedicalControl.layoutManager = LinearLayoutManager(this)
        binding.rvMedicalControl.adapter = medicalControlAdapter
    }

    private fun showAddMedicationDialog() {

        val nombre = intent.getStringExtra("nombre")?.takeIf { it != "No detectado" } ?: ""
        val cantidad = intent.getStringExtra("cantidad")?.takeIf { it != "No detectado" } ?: ""
        val masa = intent.getStringExtra("masa")?.takeIf { it != "No detectado" } ?: ""
        val otrosDatos = intent.getStringExtra("otrosDatos")?.takeIf { it != "No detectado" } ?: ""


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
        val etMorningTime = dialogView.findViewById<EditText>(R.id.etMedicationMorningTime)
        val etAfternoonTime = dialogView.findViewById<EditText>(R.id.etMedicationAfternoonTime)
        val etNightTime = dialogView.findViewById<EditText>(R.id.etMedicationNightTime)


        etName.setText(nombre)
        etQuantity.setText(cantidad.toString())
        etDosis.setText(masa)
        etObservations.setText(otrosDatos)

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
            val nurse = usuarioActual
            val dosis = etDosis.text.toString().trim().toIntOrNull()
            val morningTime = etMorningTime.text.toString().trim()
            val afternoonTime = etAfternoonTime.text.toString().trim()
            val nightTime = etNightTime.text.toString().trim()

            // Validación de campos obligatorios
            if (name.isEmpty() || lot.isEmpty() || invima.isEmpty() || quantity == null || dosis == null ||
                expirationDate.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || nurse.isEmpty() ||
                morningTime.isEmpty() || afternoonTime.isEmpty() || nightTime.isEmpty()) {

                Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                val newMedication = mapOf(
                    "nombre" to name,
                    "lote" to lot,
                    "invima" to invima,
                    "cantidad" to quantity,
                    "fecha_vencimiento" to expirationDate,
                    "fecha_inicio" to startDate,
                    "fecha_fin" to endDate,
                    "observaciones" to observations,
                    "enfermero" to "Auxiliar: $nurse",
                    "dosis" to dosis,
                    "hora_mañana" to morningTime,
                    "hora_tarde" to afternoonTime,
                    "hora_noche" to nightTime
                )

                val db = FirebaseFirestore.getInstance()
                val patientRef = db.collection("Pacientes").document(patientId)

                // Obtener la lista actual de medicamentos del paciente y agregar el nuevo medicamento
                patientRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val existingMedications = document.get("medicamentos") as? MutableList<Map<String, Any>> ?: mutableListOf()
                        existingMedications.add(newMedication)

                        // Actualizar Firestore con la nueva lista de medicamentos
                        patientRef.update("medicamentos", existingMedications)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Medicamento agregado correctamente", Toast.LENGTH_SHORT).show()

                                // Agregar a la lista local y actualizar la UI
                                medicationList.add(
                                    MedicalControl(
                                        name, lot, invima, quantity, expirationDate, startDate, endDate,
                                        observations, "Auxiliar: $nurse", morningTime, afternoonTime, nightTime, dosis
                                    )
                                )
                                medicalControlAdapter.notifyItemInserted(medicationList.size - 1)

                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }

        // Botón para cancelar
        dialogView.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun fetchMedicationsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val patientRef = db.collection("Pacientes").document(patientId)

        patientRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val medications = document.get("medicamentos") as? List<Map<String, Any>> ?: emptyList()

                medicationList.clear()

                for (medication in medications) {
                    val name = medication["nombre"] as? String ?: "Desconocido"
                    val lot = medication["lote"] as? String ?: "N/A"
                    val invima = medication["invima"] as? String ?: "N/A"
                    val quantity = (medication["cantidad"] as? Long)?.toInt() ?: 0
                    val expirationDate = medication["fecha_vencimiento"] as? String ?: "N/A"
                    val startDate = medication["fecha_inicio"] as? String ?: "N/A"
                    val endDate = medication["fecha_fin"] as? String ?: "N/A"
                    val observations = medication["observaciones"] as? String ?: "N/A"
                    val nurse = usuarioActual
                    val dose = (medication["dosis"] as? Long)?.toInt() ?: 0
                    val morningTime = medication["hora_mañana"] as? String ?: "-"
                    val afternoonTime = medication["hora_tarde"] as? String ?: "-"
                    val nightTime = medication["hora_noche"] as? String ?: "-"

                    val newMedication = MedicalControl(
                        name = name,
                        lot = lot,
                        invima = invima,
                        quantity = quantity,
                        expirationDate = expirationDate,
                        startDate = startDate,
                        endDate = endDate,
                        observations = observations,
                        nurse = nurse,
                        morningTime = morningTime,
                        afternoonTime = afternoonTime,
                        nightTime = nightTime,
                        dosis = dose
                    )

                    medicationList.add(newMedication)
                }

                // Notificar cambios en la lista
                medicalControlAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No se encontraron medicamentos para este paciente", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al obtener medicamentos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        Log.d("Firebase", "UID del usuario autenticado: $uid")

        // 🔥 Corregimos la referencia según la estructura: user -> users -> {UID}
        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Intentamos obtener el nombre desde ambas posibles claves
                    val nombreUsuario = snapshot.child("nombre").value as? String
                        ?: snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido"

                    Log.d("Firebase", "Nombre obtenido de la base de datos: $nombreUsuario")
                    callback(nombreUsuario)
                } else {
                    Log.e("Firebase", "No se encontró el usuario en la base de datos.")
                    callback("Desconocido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener el nombre: ${error.message}")
                callback("Desconocido")
            }
        })
    }


}
