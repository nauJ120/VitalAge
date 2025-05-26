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
import com.example.vitalage.adapters.UserAdapter
import com.example.vitalage.databinding.ActivityUserManagementBinding
import com.example.vitalage.generales.IniciarSesionActivity
import com.example.vitalage.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var userAdapter: UserAdapter
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child("users")
    private var usersList = mutableListOf<User>()
    private var usuarioActual: String = "Desconocido"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        binding.recyclerUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(usersList, this::editUser, this::deleteUser)
        binding.recyclerUsers.adapter = userAdapter

        // Obtener usuarios desde Firebase
        fetchUsersFromDatabase()

        // Botón para agregar nuevo usuario
        binding.fabAddUser.setOnClickListener {
            startActivity(Intent(this, UserFormActivity::class.java))
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

    private fun fetchUsersFromDatabase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val uid = userSnapshot.key ?: continue

                    val user = User(
                        id = uid, //
                        nombre_usuario = userSnapshot.child("nombre_usuario").getValue(String::class.java) ?: "",
                        correo = userSnapshot.child("correo").getValue(String::class.java) ?: "",
                        identificacion = userSnapshot.child("identificacion").getValue(String::class.java) ?: "",
                        rol = userSnapshot.child("rol").getValue(String::class.java) ?: ""
                    )

                    // 🔥 Filtrar administradores para que no aparezcan en la lista
                    if (user.rol != "Administrador") {
                        usersList.add(user)
                    }
                }
                userAdapter.updateData(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener usuarios", error.toException())
            }
        })
    }

    private fun editUser(user: User) {
        val intent = Intent(this, UserFormActivity::class.java).apply {
            putExtra("user_id", user.id)
        }
        startActivity(intent)
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Seguro que deseas eliminar a ${user.nombre_usuario}?")
            .setPositiveButton("Sí") { _, _ ->
                databaseRef.child(user.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
