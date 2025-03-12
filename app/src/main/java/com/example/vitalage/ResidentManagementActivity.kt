package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.databinding.ActivityResidentManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ResidentManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentManagementBinding
    private lateinit var residentAdapter: ResidentAdapter // 🔥 Usa ResidentAdapter en lugar de PatientAdapter
    private val db = FirebaseFirestore.getInstance()
    private var residentsList = mutableListOf<Resident>() // 🔥 Lista de residentes
    private var residentsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView con ResidentAdapter
        binding.recyclerResidents.layoutManager = LinearLayoutManager(this)
        residentAdapter = ResidentAdapter(
            residentsList,
            onEditClick = { resident -> editResident(resident) },
            onDeleteClick = { resident -> deleteResident(resident) }
        )
        binding.recyclerResidents.adapter = residentAdapter

        // Obtener residentes desde Firestore
        fetchResidentsFromFirestore()

        // Botón para agregar un nuevo residente
        binding.fabAddResident.setOnClickListener {
            startActivity(Intent(this, ResidentFormActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }
    }

    private fun fetchResidentsFromFirestore() {
        residentsListener = db.collection("Pacientes")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "Error al obtener residentes", error)
                    return@addSnapshotListener
                }

                residentsList.clear()
                for (document in snapshots!!) {
                    val resident = Resident(
                        name = document.getString("nombre") ?: "Desconocido",
                        id = document.id,
                        gender = document.getString("sexo") ?: "No especificado",
                        age = document.getLong("edad")?.toInt() ?: 0
                    )
                    residentsList.add(resident)
                }
                residentAdapter.updateData(residentsList)
            }
    }

    private fun editResident(resident: Resident) {
        val intent = Intent(this, ResidentFormActivity::class.java).apply {
            putExtra("resident_id", resident.id)
        }
        startActivity(intent)
    }

    private fun deleteResident(resident: Resident) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar residente")
            .setMessage("¿Seguro que deseas eliminar a ${resident.name}?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("Pacientes").document(resident.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Residente eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        residentsListener?.remove()
    }
}
