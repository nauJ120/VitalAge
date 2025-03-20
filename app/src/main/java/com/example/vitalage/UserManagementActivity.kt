package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalage.databinding.ActivityUserManagementBinding
import com.example.vitalage.model.User
import com.google.firebase.database.*

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var userAdapter: UserAdapter
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child("users")
    private var usersList = mutableListOf<User>()

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
    }

    private fun fetchUsersFromDatabase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val uid = userSnapshot.key ?: continue // 🔥 Obtener UID de Firebase

                    val user = User(
                        id = uid, // 🔥 Guardamos el UID en vez de identificacion
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
            putExtra("user_id", user.id) // 🔥 Pasar el UID, no identificacion
        }
        startActivity(intent)
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Seguro que deseas eliminar a ${user.nombre_usuario}?")
            .setPositiveButton("Sí") { _, _ ->
                databaseRef.child(user.id).removeValue() // 🔥 Eliminar con UID
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
}
