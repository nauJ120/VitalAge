package com.example.vitalage

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.util.Calendar

class MedicalControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalControlBinding
    private lateinit var medicalControlAdapter: MedicalControlAdapter

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    private var usuarioActual: String = "Desconocido"

    // Lista inicial de medicamentos (simulada)
    private val medicationList = mutableListOf<MedicalControl>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Usuario: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        Log.d("DEBUG", "📌 Id del paciente: ${patientId}")



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

        etExpirationDate.setOnClickListener {
            showDatePicker(etExpirationDate)
        }

        etStartDate.setOnClickListener {
            showDatePicker(etStartDate)
        }

        etEndDate.setOnClickListener {
            showDatePicker(etEndDate)
        }

        etMorningTime.setOnClickListener {
            showTimePicker(etMorningTime)
        }
        etAfternoonTime.setOnClickListener {
            showTimePicker(etAfternoonTime)
        }
        etNightTime.setOnClickListener {
            showTimePicker(etNightTime)
        }


        val autoCompleteMedicamento = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteMedicamento)
        val medicamentosGenerales = mutableListOf<String>()
        val medicamentosMap = mutableMapOf<String, Map<String, Any>>() // nombre → info

        val db = FirebaseFirestore.getInstance()
        db.collection("Medicamentos")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val nombre = doc.getString("nombre") ?: continue
                    medicamentosGenerales.add(nombre)
                    medicamentosMap[nombre] = doc.data
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicamentosGenerales)
                autoCompleteMedicamento.setAdapter(adapter)

                autoCompleteMedicamento.setOnItemClickListener { parent, _, position, _ ->
                    val selectedNombre = parent.getItemAtPosition(position) as String
                    val selectedData = medicamentosMap[selectedNombre]

                    selectedData?.let {
                        etName.setText(it["nombre"] as? String ?: "")
                        etInvima.setText(it["invima"] as? String ?: "")
                        etQuantity.setText((it["cantidad"] as? Long)?.toString() ?: "")
                        etDosis.setText((it["dosis"] as? Long)?.toString() ?: "")
                        etObservations.setText(it["observaciones"] as? String ?: "")
                    }
                }
            }



        etName.setText(nombre)
        etQuantity.setText(cantidad.toString())
        etDosis.setText(masa)
        etObservations.setText(otrosDatos)

        // Botón para agregar el medicamento
        dialogView.findViewById<Button>(R.id.btnDialogAdd).setOnClickListener {
            val name = etName.text.toString().trim()
            val lot = etLot.text.toString().trim()
            val invima = etInvima.text.toString().trim()
            val dosis = etDosis.text.toString().trim().toIntOrNull()
            val cantidad = etQuantity.text.toString().trim().toIntOrNull()
            val observaciones = etObservations.text.toString().trim()
            val fechaVencimiento = etExpirationDate.text.toString().trim()
            val fechaInicio = etStartDate.text.toString().trim()
            val fechaFin = etEndDate.text.toString().trim()
            val horaMañana = etMorningTime.text.toString().trim()
            val horaTarde = etAfternoonTime.text.toString().trim()
            val horaNoche = etNightTime.text.toString().trim()
            val auxiliar = usuarioActual

            if (name.isEmpty() || invima.isEmpty() || dosis == null || cantidad == null ||
                lot.isEmpty() || fechaVencimiento.isEmpty() || fechaInicio.isEmpty() || fechaFin.isEmpty() ||
                horaMañana.isEmpty() || horaTarde.isEmpty() || horaNoche.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val globalId = "${name.lowercase()}_${dosis}mg"

            val globalMedData = mapOf(
                "id" to globalId,
                "nombre" to name,
                "invima" to invima,
                "dosis" to dosis,
                "cantidad" to cantidad,
                "observaciones" to observaciones
            )

            val nuevoRegistro = mapOf(
                "medicamento_id" to globalId,
                "lote" to lot,
                "fecha_vencimiento" to fechaVencimiento,
                "fecha_inicio" to fechaInicio,
                "fecha_fin" to fechaFin,
                "hora_mañana" to horaMañana,
                "hora_tarde" to horaTarde,
                "hora_noche" to horaNoche,
                "auxiliar" to auxiliar,
                "cantidad_paciente" to cantidad
            )

            // 1. Crear medicamento global si no existe
            db.collection("Medicamentos").document(globalId).get().addOnSuccessListener { globalDoc ->
                if (!globalDoc.exists()) {
                    db.collection("Medicamentos").document(globalId).set(globalMedData)
                }

                // 2. Obtener y actualizar los medicamentos del paciente
                val patientRef = db.collection("Pacientes").document(patientId)
                patientRef.get().addOnSuccessListener { doc ->
                    val existingList = doc.get("medicamentos") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val updatedList = mutableListOf<Map<String, Any>>()
                    var yaExistia = false

                    for (med in existingList) {
                        if ((med["medicamento_id"] as? String) == globalId) {
                            val cantidadActual = (med["cantidad_paciente"] as? Long ?: 0)

                            val actualizado = mutableMapOf<String, Any>(
                                "medicamento_id" to globalId,
                                "lote" to lot,
                                "fecha_vencimiento" to fechaVencimiento,
                                "fecha_inicio" to fechaInicio,
                                "fecha_fin" to fechaFin,
                                "hora_mañana" to horaMañana,
                                "hora_tarde" to horaTarde,
                                "hora_noche" to horaNoche,
                                "auxiliar" to auxiliar,
                                "cantidad_paciente" to cantidadActual + cantidad
                            )

                            updatedList.add(actualizado)
                            yaExistia = true
                        } else {
                            updatedList.add(med)
                        }
                    }

                    if (!yaExistia) {
                        updatedList.add(nuevoRegistro)
                    }

                    // 3. Guardar cambios
                    patientRef.update("medicamentos", updatedList)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Medicamento registrado correctamente", Toast.LENGTH_SHORT).show()
                            fetchMedicationsFromFirestore()
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.d("DEBUG", "📌 Error: ${e.message}")
                        }
                }
            }
        }



        // Botón para cancelar
        dialogView.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
            targetEditText.setText(selectedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun showTimePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val amPm = if (selectedHour < 12) "AM" else "PM"
            val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val minuteFormatted = String.format("%02d", selectedMinute)
            val formattedTime = "$hourFormatted:$minuteFormatted $amPm"
            targetEditText.setText(formattedTime)
        }, hour, minute, false) // false = formato 12h

        timePicker.show()
    }




    private fun fetchMedicationsFromFirestore() {
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        Log.d("DEBUG", "📌 patientId recibido: $patientId")
        val db = FirebaseFirestore.getInstance()
        val patientRef = db.collection("Pacientes").document(patientId)

        patientRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val medicamentosRelacionados = document.get("medicamentos") as? List<Map<String, Any>> ?: emptyList()
                medicationList.clear()

                for (relacion in medicamentosRelacionados) {
                    val medicamentoId = relacion["medicamento_id"] as? String ?: continue

                    // Consultar datos generales del medicamento
                    db.collection("Medicamentos").document(medicamentoId).get()
                        .addOnSuccessListener { medDoc ->
                            if (medDoc.exists()) {
                                val datosGlobales = medDoc.data ?: return@addOnSuccessListener

                                // Combinar con datos específicos
                                val medicamento = MedicalControl(
                                    name = datosGlobales["nombre"] as? String ?: "Desconocido",
                                    lot = relacion["lote"] as? String ?: "-",
                                    invima = datosGlobales["invima"] as? String ?: "-",
                                    quantity = datosGlobales["cantidad"]?.toString()?.toIntOrNull() ?: 0,
                                    expirationDate = relacion["fecha_vencimiento"] as? String ?: "-",
                                    startDate = relacion["fecha_inicio"] as? String ?: "-",
                                    endDate = relacion["fecha_fin"] as? String ?: "-",
                                    observations = datosGlobales["observaciones"] as? String ?: "-",
                                    nurse = relacion["auxiliar"] as? String ?: "Desconocido",
                                    morningTime = relacion["hora_mañana"] as? String ?: "-",
                                    afternoonTime = relacion["hora_tarde"] as? String ?: "-",
                                    nightTime = relacion["hora_noche"] as? String ?: "-",
                                    dosis = datosGlobales["dosis"]?.toString()?.toIntOrNull() ?: 0
                                )

                                medicationList.add(medicamento)
                                medicalControlAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener medicamentos", Toast.LENGTH_SHORT).show()
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
