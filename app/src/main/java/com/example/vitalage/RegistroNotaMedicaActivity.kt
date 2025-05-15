package com.example.vitalage

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegistroNotaMedicaActivity : AppCompatActivity() {

    private lateinit var patientId: String
    private lateinit var patientName: String
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_nota_medica)

        // Botón de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Obtener ID del paciente desde intent
        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: "Paciente"

        db = FirebaseFirestore.getInstance()

        // Spinners
        val spFinalidad = findViewById<Spinner>(R.id.spFinalidadConsulta)
        val spCausa = findViewById<Spinner>(R.id.spCausaExterna)
        val spTipoDiagnostico = findViewById<Spinner>(R.id.spTipoDiagnostico)

        spFinalidad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(
            "10 - No aplica", "11 - Accidente de trabajo", "12 - Accidente de tránsito", "13 - Enfermedad general"
        ))

        spCausa.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(
            "10 - No aplica", "11 - Causa laboral", "12 - Causa relacionada", "13 - Otro"
        ))

        spTipoDiagnostico.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(
            "Presuntivo", "Confirmado"
        ))

        // Botón guardar
        val btnGuardar = findViewById<Button>(R.id.btnGuardarNota)
        btnGuardar.setOnClickListener {
            guardarNotaMedica()
        }
    }

    private fun guardarNotaMedica() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val fechaActual = sdf.format(Date())

        val nota = hashMapOf(
            "fecha" to fechaActual,
            "medico" to "Nombre del médico", // Puedes reemplazarlo por quien esté logueado
            "finalidad_consulta" to findViewById<Spinner>(R.id.spFinalidadConsulta).selectedItem.toString(),
            "causa_externa" to findViewById<Spinner>(R.id.spCausaExterna).selectedItem.toString(),
            "motivo" to findViewById<EditText>(R.id.etMotivo).text.toString(),
            "enfermedad_actual" to findViewById<EditText>(R.id.etEnfermedadActual).text.toString(),
            "ayuda_diagnostica" to findViewById<EditText>(R.id.etAyudaDiagnostica).text.toString(),
            "antecedentes" to mapOf(
                "ginecologico" to findViewById<EditText>(R.id.etAntecedenteGinecologico).text.toString(),
                "alergico" to findViewById<EditText>(R.id.etAntecedenteAlergico).text.toString(),
                "quirurgico" to findViewById<EditText>(R.id.etAntecedenteQuirurgico).text.toString(),
                "patologico" to findViewById<EditText>(R.id.etAntecedentePatologico).text.toString(),
                "farmacologico" to findViewById<EditText>(R.id.etAntecedenteFarmacologico).text.toString(),
                "toxicologico" to findViewById<EditText>(R.id.etAntecedenteToxicologico).text.toString(),
                "otro" to findViewById<EditText>(R.id.etAntecedenteOtro).text.toString(),
                "familiar" to findViewById<EditText>(R.id.etAntecedenteFamiliar).text.toString()
            ),
            "revision_sistemas" to findViewById<EditText>(R.id.etRevisionSistemas).text.toString(),
            "examen_fisico" to findViewById<EditText>(R.id.etExamenFisico).text.toString(),
            "analisis" to findViewById<EditText>(R.id.etAnalisis).text.toString(),
            "dx_principal" to findViewById<EditText>(R.id.etDxPrincipal).text.toString(),
            "tipo_diagnostico" to findViewById<Spinner>(R.id.spTipoDiagnostico).selectedItem.toString(),
            "tratamiento" to findViewById<EditText>(R.id.etTratamiento).text.toString(),
            "recomendacion" to findViewById<EditText>(R.id.etRecomendacion).text.toString()
        )

        db.collection("Pacientes").document(patientId)
            .update("notasMedicas", com.google.firebase.firestore.FieldValue.arrayUnion(nota))
            .addOnSuccessListener {
                Toast.makeText(this, "Nota médica guardada correctamente", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar la nota: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
