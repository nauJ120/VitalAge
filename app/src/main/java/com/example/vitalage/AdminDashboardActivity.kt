package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val tvResidentesActivos = findViewById<TextView>(R.id.tvResidentesActivos)
        val tvPersonalSalud = findViewById<TextView>(R.id.tvPersonalSalud)
        val layoutNotas = findViewById<LinearLayout>(R.id.layout_alert_notes)
        val layoutMeds = findViewById<LinearLayout>(R.id.layout_alert_meds)

        // 🔹 Botón de volver
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // 🔹 Footer
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, MenuAdminActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            Toast.makeText(this, "Perfil en construcción", Toast.LENGTH_SHORT).show()
        }

        val db = FirebaseFirestore.getInstance()

        // 🔹 Indicadores
        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                tvResidentesActivos.text = result.size().toString()
            }
            .addOnFailureListener {
                Log.e("Dashboard", "Error pacientes: ${it.message}")
                tvResidentesActivos.text = "0"
            }

        val dbRealtime = FirebaseDatabase.getInstance().getReference("user/users")
        dbRealtime.get().addOnSuccessListener { snapshot ->
            var count = 0
            for (userSnap in snapshot.children) {
                val rol = userSnap.child("rol").value as? String ?: ""
                if (rol.equals("enfermera", ignoreCase = true) || rol.equals("médico", ignoreCase = true)) count++
            }
            tvPersonalSalud.text = count.toString()
        }.addOnFailureListener {
            Log.e("Dashboard", "Error personal salud: ${it.message}")
            tvPersonalSalud.text = "0"
        }

        // 🔹 Notas recientes (últimos 7 días)
        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                layoutNotas.removeAllViews()
                for (doc in result) {
                    val notas = doc.get("notasEnfermeria") as? List<Map<String, Any>> ?: continue
                    for (nota in notas) {
                        val fecha = nota["fecha"] as? String ?: continue
                        val enfermera = nota["enfermera"] as? String ?: ""
                        val titulo = nota["titulo"] as? String ?: ""
                        val descripcion = nota["descripcion"] as? String ?: ""

                        val card = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(16, 16, 16, 16)
                            setBackgroundResource(android.R.color.white)
                            elevation = 4f
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 0, 0, 16)
                            layoutParams = params
                        }

                        val icon = ImageView(this).apply {
                            setImageResource(R.drawable.ic_note)
                            setColorFilter(getColor(R.color.md_blue_500))
                            layoutParams = LinearLayout.LayoutParams(48, 48)
                        }

                        val tvTitulo = TextView(this).apply {
                            text = titulo.ifBlank { "Sin título" }
                            textSize = 16f
                            setTextColor(getColor(R.color.black))
                            setPadding(0, 8, 0, 4)
                        }

                        val tvDetalle = TextView(this).apply {
                            text = "$fecha - $enfermera: ${descripcion.take(100)}"
                            textSize = 14f
                            setTextColor(getColor(R.color.black))
                        }

                        card.addView(icon)
                        card.addView(tvTitulo)
                        card.addView(tvDetalle)

                        layoutNotas.addView(card)
                    }
                }
            }

        // 🔹 Medicamentos por vencer (próximos 7 días)
        layoutMeds.removeAllViews()

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hoy = Calendar.getInstance().time
        val limite = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time

        db.collection("Pacientes")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val nombrePaciente = doc.getString("nombre") ?: "Paciente sin nombre"
                    val meds = doc.get("medicamentos") as? List<Map<String, Any>> ?: continue

                    for (med in meds) {
                        val nombreMed = med["nombre"] as? String ?: "Medicamento sin nombre"
                        val fechaStr = med["fecha_vencimiento"] as? String ?: continue

                        try {
                            val fecha = sdf.parse(fechaStr)
                            if (fecha != null && fecha.after(hoy) && fecha.before(limite)) {
                                val view = TextView(this).apply {
                                    text = "$nombrePaciente - $nombreMed vence: $fechaStr"
                                    textSize = 14f
                                    setTextColor(getColor(R.color.black))
                                    setPadding(0, 0, 0, 8)
                                }
                                layoutMeds.addView(view)
                            }
                        } catch (e: Exception) {
                            Log.e("Dashboard", "Error parsing fecha: $fechaStr")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar medicamentos por vencer", Toast.LENGTH_SHORT).show()
                Log.e("Dashboard", "Error medicamentos: ${it.message}")
            }
    }
}
