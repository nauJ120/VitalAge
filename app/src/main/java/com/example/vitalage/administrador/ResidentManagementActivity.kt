package com.example.vitalage.administrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.adapters.Resident
import com.example.vitalage.adapters.ResidentAdapter
import com.example.vitalage.databinding.ActivityResidentManagementBinding
import com.example.vitalage.generales.IniciarSesionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ResidentManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentManagementBinding
    private lateinit var residentAdapter: ResidentAdapter // 🔥 Usa ResidentAdapter en lugar de PatientAdapter
    private val db = FirebaseFirestore.getInstance()
    private var residentsList = mutableListOf<Resident>() // 🔥 Lista de residentes
    private var residentsListener: ListenerRegistration? = null
    private var usuarioActual: String = "Desconocido"

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
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.tvUser)
            tvUser.text = "Administrador: $usuarioActual"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
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

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            // Usuario no logeado
            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        val userId = user.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("user/users/$userId")

        dbRef.get().addOnSuccessListener { snapshot ->
            val rol = snapshot.child("rol").value.toString()

            // Aquí comparas según el rol esperado
            if (rol != "Administrador") {
                Toast.makeText(this, "Acceso restringido a administradores", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar el rol", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
        }
    }
}
