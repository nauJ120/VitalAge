package com.example.vitalage

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.clases.Terapia
import com.example.vitalage.databinding.ActivityTerapiaFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class TerapiaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTerapiaFormBinding
    private val db = FirebaseFirestore.getInstance()

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTerapiaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configurar Selección de Fecha
        binding.etFecha.setOnClickListener { showDatePicker() }

        // Guardar Terapia
        binding.btnGuardar.setOnClickListener { saveTerapia() }

        // Cancelar
        binding.btnCancelar.setOnClickListener { finish() }

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvUser.text = "Enfermera: $usuarioActual"
        }
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.etFecha.setText(selectedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun saveTerapia() {
        val tipo = binding.spTipoTerapia.text?.toString()?.trim() ?: ""
        val fecha = binding.etFecha.text.toString().trim()
        val realizada = binding.switchRealizada.isChecked
        val encargado = FirebaseAuth.getInstance().currentUser?.displayName ?: "Desconocido"

        if (tipo.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔥 Obtener el ID del residente desde el Intent
        if (patientId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se encontró el residente", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear una nueva terapia como mapa
        val nuevaTerapia = hashMapOf(
            "tipo" to tipo,
            "fecha" to fecha,
            "encargado" to encargado,
            "realizada" to realizada
        )

        // 🔥 Agregar la terapia al array "terapias" dentro del documento del residente
        val pacienteRef = db.collection("Pacientes").document(patientId)
        pacienteRef.update("terapias", FieldValue.arrayUnion(nuevaTerapia))
            .addOnSuccessListener {
                Toast.makeText(this, "Terapia agregada con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar terapia: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerNombreUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("Firebase", "No se encontró un usuario autenticado.")
            callback("Desconocido")
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombreUsuario = snapshot.child("nombre").value as? String
                        ?: snapshot.child("nombre_usuario").value as? String
                        ?: "Desconocido"

                    callback(nombreUsuario)
                } else {
                    callback("Desconocido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback("Desconocido")
            }
        })
    }

}
