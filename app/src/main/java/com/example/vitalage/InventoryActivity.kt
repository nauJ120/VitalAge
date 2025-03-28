package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.MedicationAdapter
import com.example.vitalage.databinding.ActivityInventoryBinding
import com.example.vitalage.model.Medication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var medicationAdapter: MedicationAdapter
    private val medicationList = mutableListOf<Medication>()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var patientId: String

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener nombre de usuario actual
        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Usuario: $usuarioActual"
        }
        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        // Obtener datos del Intent
        val patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        val patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        val patientAge = intent.getIntExtra("patient_age", 0)

        // Mostrar datos del paciente en la UI
        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"


        val btnHomeContainer = findViewById<LinearLayout>(R.id.btnHomeContainer)

        btnHomeContainer.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java) // Reemplaza "NuevaActividad" con el nombre de tu actividad destino
            startActivity(intent)
        }

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar medicamentos desde Firestore
        fetchMedicationsFromFirestore()

        binding.btnApplyFilter.setOnClickListener {
            val query = binding.etSearchMedication.text.toString().trim()
            applyFilter(query)
        }

        binding.btnResetFilter.setOnClickListener {
            resetFilter()
        }
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
                    val relaciones = document.get("medicamentos") as? List<Map<String, Any>> ?: emptyList()
                    medicationList.clear()

                    // Para cada relación en el paciente
                    for (relacion in relaciones) {
                        val medicamentoId = relacion["medicamento_id"] as? String ?: continue
                        val cantidadPaciente = (relacion["cantidad_paciente"] as? Long)?.toInt() ?: 0
                        val auxiliar = relacion["auxiliar"] as? String ?: "N/A"
                        val fechaInicio = relacion["fecha_inicio"] as? String ?: "-"
                        val fechaFin = relacion["fecha_fin"] as? String ?: "-"
                        val fechaVencimiento = relacion["fecha_vencimiento"] as? String ?: "-"
                        val horaMañana = relacion["hora_mañana"] as? String ?: "-"
                        val horaTarde = relacion["hora_tarde"] as? String ?: "-"
                        val horaNoche = relacion["hora_noche"] as? String ?: "-"
                        val lote = relacion["lote"] as? String ?: "-"

                        // Consultar los datos generales del medicamento desde la colección global
                        firestore.collection("Medicamentos").document(medicamentoId).get()
                            .addOnSuccessListener { medDoc ->
                                if (medDoc.exists()) {
                                    val nombre = medDoc.getString("nombre") ?: "Desconocido"
                                    val invima = medDoc.getString("invima") ?: "-"
                                    val observaciones = medDoc.getString("observaciones") ?: "-"
                                    val dosis = (medDoc.getLong("dosis") ?: 0).toInt()

                                    val medicamento = Medication(
                                        nombre = nombre,
                                        cantidad = cantidadPaciente,
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

                                    medicationList.add(medicamento)
                                    medicationAdapter.notifyItemInserted(medicationList.size - 1)
                                }
                            }
                    }
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
            .setMessage("Cantidad disponible: ${medication.cantidad}")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val administeredQuantity = etQuantity.text.toString().trim().toIntOrNull()

                when {
                    administeredQuantity == null || administeredQuantity <= 0 -> {
                        Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
                    }
                    administeredQuantity > medication.cantidad -> {
                        Toast.makeText(this, "No hay suficiente cantidad disponible", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                        updateMedicationQuantity(medication, administeredQuantity, usuarioActual)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)

        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun updateMedicationQuantity(medication: Medication, administeredQuantity: Int, usuario: String) {
        val patientRef = firestore.collection("Pacientes").document(patientId)

        // Obtener la lista actual de medicamentos
        patientRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val medicamentos = document.get("medicamentos") as? MutableList<Map<String, Any>>
                if (medicamentos != null) {
                    val updatedMedications = medicamentos.map { med ->
                        if (med["nombre"] == medication.nombre) {
                            // Convertir el mapa a mutable y actualizar la cantidad
                            med.toMutableMap().apply {
                                val cantidadActual = (this["cantidad"] as? Long)?.toInt() ?: 0
                                put("cantidad", cantidadActual - administeredQuantity) // Descontar cantidad administrada
                            }
                        } else {
                            med
                        }
                    }

                    // Guardar la lista actualizada en Firestore
                    patientRef.update("medicamentos", updatedMedications)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Medicamento administrado correctamente", Toast.LENGTH_SHORT).show()

                            // Actualizar en la lista local
                            val medicamentoEncontrado = medicationList.find { it.nombre == medication.nombre }
                            medicamentoEncontrado?.let {
                                it.cantidad = (it.cantidad ?: 0) - administeredQuantity
                                medicationAdapter.notifyDataSetChanged()
                            }


                            // Guardar en el historial de administración de dosis
                            saveDoseHistory(medication, administeredQuantity, usuario)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al actualizar cantidad: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al obtener paciente: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun saveDoseHistory(medication: Medication, administeredQuantity: Int, usuario: String) {
        val patientRef = firestore.collection("Pacientes").document(patientId)

        val doseRecord = hashMapOf(
            "medicamento" to medication.nombre,
            "cantidad" to administeredQuantity,
            "dosis" to medication.dosis,
            "fecha_hora" to com.google.firebase.Timestamp.now(),
            "usuario" to usuario,
            "observaciones" to "Dosis administrada correctamente"
        )

        patientRef.update("historial_dosis", FieldValue.arrayUnion(doseRecord))
            .addOnSuccessListener {
                Toast.makeText(this, "Historial actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar historial: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun applyFilter(query: String) {
        if (query.isNotEmpty()) {
            val filteredList = medicationList.filter { it.nombre.contains(query, ignoreCase = true) }
            medicationAdapter.updateData(filteredList)
        } else {
            Toast.makeText(this, "Ingrese un nombre para filtrar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetFilter() {
        binding.etSearchMedication.text.clear()
        medicationAdapter.updateData(medicationList)
    }



}
