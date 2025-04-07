package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.vitalage.databinding.EscaneadoBinding




class EscaneoActivity: AppCompatActivity() {

    private lateinit var escaneadoBinding: EscaneadoBinding
    private val TAG = EscaneoActivity::class.java.simpleName
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        escaneadoBinding = EscaneadoBinding.inflate(layoutInflater)
        setContentView(escaneadoBinding.root)
        enableEdgeToEdge()

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        val botoncancelar = findViewById<AppCompatButton>(R.id.buttonCamera)

        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"

        val nombre = intent.getStringExtra("nombreMedicamento") ?: "No detectado"
        val cantidad = intent.getStringExtra("cantidad") ?: "No detectado"
        val masa = intent.getStringExtra("masa") ?: "No detectado"
        val maxLongitud = 450  // Ajusta el límite según lo necesites
        val otrosDatos = intent.getStringExtra("otrosDatos")?.let {
            val datosProcesados = it.replace("\n", ", ").take(maxLongitud)
            if (it.length > maxLongitud) "$datosProcesados..." else datosProcesados
        } ?: "No detectado"


        escaneadoBinding.nombreMedicamento.text = "Nombre: $nombre\nCantidad: $cantidad\nMasa: $masa\nOtros: $otrosDatos"


        escaneadoBinding.buttoncancelar.setOnClickListener{
            val intent = Intent(this, MedicalControlActivity::class.java)
            intent.putExtra("from_scan", true)  // Enviar bandera de escaneo
            intent.putExtra("nombre", nombre)
            intent.putExtra("cantidad", cantidad)
            intent.putExtra("masa", masa)
            intent.putExtra("otrosDatos", otrosDatos)
            intent.putExtra("patient_id",patientId)
            Log.d("DEBUG", "📌 patientId recibido: $patientId")
            startActivity(intent)
        }

        escaneadoBinding.buttonCamera.setOnClickListener{
            val intent = Intent(this, FotoCamaraActivity::class.java)
            startActivity(intent)
        }

    }

}