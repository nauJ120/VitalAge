package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ScaleResultActivity : AppCompatActivity() {

    private var usuarioActual: String = "Desconocido"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scale_result)

        // Referencias a las vistas
        val tvScaleType: TextView = findViewById(R.id.tv_scale_type)
        val tvTotalScore: TextView = findViewById(R.id.tv_total_score)
        val tvEncargado: TextView = findViewById(R.id.tv_encargado)
        val tvDate: TextView = findViewById(R.id.tv_date)
        val tvGeneralMessage: TextView = findViewById(R.id.tv_general_message)
        val tvSpecificMessage: TextView = findViewById(R.id.tv_specific_message)
        val btnBack: Button = findViewById(R.id.btn_back)
        val btnBack2: ImageView = findViewById(R.id.btnBack)

        // Obtener datos del Intent
        val scaleType = intent.getStringExtra("scale_type") ?: "Sin información"
        val totalScore = intent.getIntExtra("score", 0)
        val encargado = intent.getStringExtra("encargado") ?: "Sin información"
        val fecha = intent.getStringExtra("fecha") ?: "Sin información"

        // Configurar vistas con los datos
        tvScaleType.text = "Escala: $scaleType"
        tvTotalScore.text = "Puntaje Total: $totalScore"
        tvEncargado.text = "Encargado: $encargado"
        tvDate.text = "Fecha: $fecha"

        // Mostrar mensajes según el tipo de escala
        val (generalMessage, specificMessage) = getMessagesForScale(scaleType, totalScore)
        tvGeneralMessage.text = generalMessage
        tvSpecificMessage.text = specificMessage

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            // Utilizamos findViewById para acceder al TextView
            val tvSubtitulo = findViewById<TextView>(R.id.tvSubtitulo)
            tvSubtitulo.text = "Enfermera: $usuarioActual"
        }

        // Configurar botón de volver
        btnBack.setOnClickListener {
            finish()
        }
        btnBack2.setOnClickListener {
            finish()
        }
        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}
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


    private fun getMessagesForScale(scaleType: String, score: Int): Pair<String, String> {
        return when (scaleType) {
            "Braden" -> {
                val generalMessage = "La Escala de Braden mide el riesgo de desarrollar úlceras por presión. Según el resultado, se pueden tomar medidas preventivas."
                val specificMessage = when {
                    score in 15..18 -> "Riesgo leve: El paciente tiene un riesgo leve de desarrollar úlceras por presión. Se recomienda monitorear regularmente la piel y mantener una higiene adecuada."
                    score in 13..14 -> "Riesgo moderado: El paciente presenta un riesgo moderado de úlceras por presión. Es importante realizar cambios posturales frecuentes y evaluar la superficie del colchón o silla."
                    score in 10..12 -> "Riesgo alto: El paciente tiene un alto riesgo de úlceras por presión. Implementar medidas preventivas intensivas, como cambios posturales cada 2 horas y uso de colchones especiales."
                    score <= 9 -> "Riesgo muy alto: Riesgo crítico de desarrollar úlceras por presión. Es esencial un monitoreo continuo, cuidados avanzados y supervisión constante."
                    else -> "Puntaje fuera de rango."
                }
                generalMessage to specificMessage
            }
            "Barthel" -> {
                val generalMessage = "La Escala de Barthel evalúa la capacidad del paciente para realizar actividades de la vida diaria."
                val specificMessage = when {
                    score in 80..100 -> "Independencia: El paciente puede realizar sus actividades diarias con mínima o ninguna ayuda. Supervisión ocasional puede ser suficiente."
                    score in 60..79 -> "Dependencia leve: El paciente requiere algo de ayuda para ciertas actividades, pero puede realizar varias de manera autónoma."
                    score in 40..59 -> "Dependencia moderada: El paciente necesita asistencia significativa para realizar actividades cotidianas. Supervisión constante es importante."
                    score in 20..39 -> "Dependencia severa: El paciente depende de ayuda para la mayoría de sus actividades diarias. Un cuidador es esencial en su rutina."
                    score in 0..19 -> "Dependencia total: El paciente es completamente dependiente y necesita apoyo constante para todas las actividades diarias."
                    else -> "Puntaje fuera de rango."
                }
                generalMessage to specificMessage
            }
            "Glasgow" -> {
                val generalMessage = "La Escala de Glasgow mide el nivel de conciencia del paciente y la gravedad de lesiones cerebrales."
                val specificMessage = when {
                    score in 13..15 -> "Lesión leve o estado normal: El paciente tiene un estado consciente normal o presenta una lesión leve. Continuar con observación y seguimiento."
                    score in 9..12 -> "Lesión moderada: El paciente muestra una respuesta moderada y puede tener confusión o letargo. Se recomienda evaluación médica detallada y monitoreo cercano."
                    score in 3..8 -> "Lesión severa o coma: El paciente se encuentra en un estado crítico (coma). Es necesaria intervención médica inmediata y cuidados intensivos."
                    else -> "Puntaje fuera de rango."
                }
                generalMessage to specificMessage
            }
            else -> "Tipo de escala desconocida." to "No se pudo determinar el mensaje específico."
        }
    }
}
