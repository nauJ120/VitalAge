package com.example.vitalage.enfermera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.databinding.EscaneadoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EscaneoActivity: AppCompatActivity() {

    private lateinit var escaneadoBinding: EscaneadoBinding
    private val TAG = EscaneoActivity::class.java.simpleName
    private lateinit var patientId: String
    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        escaneadoBinding = EscaneadoBinding.inflate(layoutInflater)
        setContentView(escaneadoBinding.root)


        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            val tvUser = findViewById<TextView>(R.id.enfermera)
            tvUser.text = "Enfermera: $usuarioActual"
        }


        val botoncancelar = findViewById<AppCompatButton>(R.id.buttonCamera)

        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"

        val nombre = intent.getStringExtra("nombreMedicamento") ?: "No detectado"
        var cantidad = intent.getStringExtra("cantidad") ?: "No detectado"
        var masa = intent.getStringExtra("masa") ?: "No detectado"
        val maxLongitud = 450  // Ajusta el límite según lo necesites
        val otrosDatos = intent.getStringExtra("otrosDatos")?.let {
            val datosProcesados = it.replace("\n", ", ").take(maxLongitud)
            if (it.length > maxLongitud) "$datosProcesados..." else datosProcesados
        } ?: "No detectado"

        cantidad = Regex("\\d+").find(cantidad)?.value ?: "No detectado"
        masa = Regex("\\d+").find(masa)?.value ?: "No detectado"


        escaneadoBinding.nombreMedicamento.text = "Nombre: $nombre\nCantidad: $cantidad\nMasa: $masa\nOtros: $otrosDatos"


        escaneadoBinding.buttoncancelar.setOnClickListener{
            val intent = Intent(this, MedicalControlActivity::class.java)
            intent.putExtra("from_scan", true)  // Enviar bandera de escaneo
            intent.putExtra("nombre", nombre)
            intent.putExtra("cantidad", cantidad)
            intent.putExtra("masa", masa)
            intent.putExtra("otrosDatos", otrosDatos)
            intent.putExtra("patient_id",patientId)

            startActivity(intent)
        }

        escaneadoBinding.buttonCamera.setOnClickListener{
            val intent = Intent(this, FotoCamaraActivity::class.java)
            startActivity(intent)
        }

        escaneadoBinding.iconScan.setOnClickListener{
            finish()
        }

        escaneadoBinding.btnHome.setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        escaneadoBinding.btnProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
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