package com.example.vitalage.enfermera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.clases.Question
import com.example.vitalage.clases.Scale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.vitalage.databinding.ActivityCreateScaleBinding


class CreateScaleActivity : AppCompatActivity() {
    private lateinit var spinnerScaleType: Spinner
    private lateinit var tvEncargado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var questionsContainer: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageView

    private lateinit var patientName: String
    private lateinit var patientId: String
    private lateinit var patientGender: String
    private var patientAge: Int = 0

    private lateinit var binding: ActivityCreateScaleBinding

    private var usuarioActual: String = "Desconocido"

    private lateinit var encargado: String
    private lateinit var fecha: String


    private val scales = listOf(
        Scale(
            name = "Braden",
            questions = listOf(
                Question(
                    category = "Percepción sensorial",
                    options = listOf(
                        "1 - Completamente limitado",
                        "2 - Muy limitado",
                        "3 - Levemente limitado",
                        "4 - Sin limitaciones"
                    )
                ),
                Question(
                    category = "Humedad",
                    options = listOf(
                        "1 - Constantemente húmeda",
                        "2 - Muy húmeda",
                        "3 - Ocasionalmente húmeda",
                        "4 - Raramente húmeda"
                    )
                ),
                Question(
                    category = "Actividad",
                    options = listOf(
                        "1 - Postrado en cama",
                        "2 - En silla",
                        "3 - Deambula ocasionalmente",
                        "4 - Deambula frecuentemente"
                    )
                ),
                Question(
                    category = "Movilidad",
                    options = listOf(
                        "1 - Completamente inmóvil",
                        "2 - Muy limitada",
                        "3 - Levemente limitada",
                        "4 - Sin limitaciones"
                    )
                ),
                Question(
                    category = "Nutrición",
                    options = listOf(
                        "1 - Muy deficiente",
                        "2 - Probablemente inadecuada",
                        "3 - Adecuada",
                        "4 - Excelente"
                    )
                ),
                Question(
                    category = "Fricción y cizallamiento",
                    options = listOf(
                        "1 - Problema significativo",
                        "2 - Problema potencial",
                        "3 - Sin aparente problema"
                    )
                )
            )
        ),
        Scale(
            name = "Barthel",
            questions = listOf(
                Question(
                    category = "Comer",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Necesita ayuda",
                        "10 - Independiente"
                    )
                ),
                Question(
                    category = "Trasladarse de la silla a la cama y viceversa",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Gran ayuda",
                        "10 - Poca ayuda",
                        "15 - Independiente"
                    )
                ),
                Question(
                    category = "Aseo personal",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Independiente"
                    )
                ),
                Question(
                    category = "Uso del retrete",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Necesita ayuda",
                        "10 - Independiente"
                    )
                ),
                Question(
                    category = "Bañarse",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Independiente"
                    )
                ),
                Question(
                    category = "Desplazarse",
                    options = listOf(
                        "0 - Inmovilizado",
                        "5 - Silla de ruedas",
                        "10 - Camina con ayuda",
                        "15 - Independiente"
                    )
                ),
                Question(
                    category = "Subir escaleras",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Necesita ayuda",
                        "10 - Independiente"
                    )
                ),
                Question(
                    category = "Vestirse",
                    options = listOf(
                        "0 - Dependiente",
                        "5 - Gran ayuda",
                        "10 - Independiente"
                    )
                ),
                Question(
                    category = "Control de heces",
                    options = listOf(
                        "0 - Incontinente",
                        "5 - Ocasionalmente",
                        "10 - Continente"
                    )
                ),
                Question(
                    category = "Control de orina",
                    options = listOf(
                        "0 - Incontinente",
                        "5 - Ocasionalmente",
                        "10 - Continente"
                    )
                )
            )
        ),
        Scale(
            name = "Glasgow",
            questions = listOf(
                Question(
                    category = "Apertura ocular",
                    options = listOf(
                        "1 - Ninguna",
                        "2 - Al dolor",
                        "3 - A estímulo verbal",
                        "4 - Espontánea"
                    )
                ),
                Question(
                    category = "Respuesta verbal",
                    options = listOf(
                        "1 - Ninguna",
                        "2 - Incomprensible",
                        "3 - Incoherente",
                        "4 - Desorientada",
                        "5 - Orientada"
                    )
                ),
                Question(
                    category = "Respuesta motora",
                    options = listOf(
                        "1 - Ninguna",
                        "2 - Extensión anormal",
                        "3 - Flexión anormal",
                        "4 - Retirada al dolor",
                        "5 - Localiza el dolor",
                        "6 - Obedece órdenes"
                    )
                )
            )
        )
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_scale)

        binding = ActivityCreateScaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar vistas
        spinnerScaleType = findViewById(R.id.spinner_scale_type)
        tvEncargado = findViewById(R.id.tvSubtitulo)
        tvFecha = findViewById(R.id.tv_fecha)
        questionsContainer = findViewById(R.id.questions_container)
        btnSave = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)

        btnBack = findViewById<ImageView>(R.id.btnBack)

        patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        patientAge = intent.getIntExtra("patient_age", 0)

        binding.tvResidentName.text = patientName
        binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))}

        // Obtener fecha actual formateada
        val fechaActual = com.google.firebase.Timestamp.now().toDate()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fecha = formatoFecha.format(fechaActual)

        tvFecha.text = "Fecha: $fecha"

        // Deshabilitar el botón hasta obtener el encargado
        btnSave.isEnabled = false
        btnCancel.setOnClickListener {
            finish()
        }
        btnBack.setOnClickListener {
            finish()
        }
        // Obtener nombre del usuario actual
        obtenerNombreUsuario { nombre ->
            encargado = nombre
            tvEncargado.text = "Encargado: $encargado"

            // Ahora que tenemos el nombre del usuario, habilitamos el botón
            btnSave.isEnabled = true
        }


        // Configurar Spinner
        val scaleTypes = scales.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, scaleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerScaleType.adapter = adapter

        // Cargar preguntas dinámicamente según la escala seleccionada
        spinnerScaleType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedScale = scaleTypes[position]
                loadQuestions(selectedScale)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configurar botón Guardar
        btnSave.setOnClickListener {
            saveScale(encargado, fecha)
        }
    }

    private fun loadQuestions(scaleType: String) {
        questionsContainer.removeAllViews()

        // Encontrar la escala seleccionada
        val selectedScale = scales.find { it.name == scaleType }
        if (selectedScale == null) {
            Log.e("CreateScaleActivity", "No se encontró la escala seleccionada: $scaleType")
            return
        }

        // Generar vistas dinámicamente
        selectedScale.questions.forEach { question ->
            val textView = TextView(this).apply {
                text = question.category
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            questionsContainer.addView(textView)

            val radioGroup = RadioGroup(this)
            question.options.forEach { option ->
                val radioButton = RadioButton(this).apply {
                    text = option
                }
                radioGroup.addView(radioButton)
            }
            questionsContainer.addView(radioGroup)
        }
    }

    private fun collectAnswers(): Map<String, String> {
        val answers = mutableMapOf<String, String>()

        for (i in 0 until questionsContainer.childCount) {
            val view = questionsContainer.getChildAt(i)
            if (view is RadioGroup) {
                val selectedId = view.checkedRadioButtonId
                val questionTextView = questionsContainer.getChildAt(i - 1) as? TextView

                if (selectedId != -1 && questionTextView != null) {
                    val selectedOption = findViewById<RadioButton>(selectedId).text.toString()
                    val question = questionTextView.text.toString()
                    answers[question] = selectedOption
                }
            }
        }

        Log.d("CreateScaleActivity", "Answers collected: $answers")
        return answers
    }

    private fun saveScale(encargado: String, fecha: String) {
        val answers = collectAnswers()

        val totalQuestions = scales.find { it.name == spinnerScaleType.selectedItem.toString() }?.questions?.size ?: 0
        if (answers.size != totalQuestions) {
            Toast.makeText(this, "Por favor responde todas las preguntas antes de continuar.", Toast.LENGTH_SHORT).show()
            return
        }

        val scaleType = spinnerScaleType.selectedItem?.toString() ?: ""
        if (scaleType.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona un tipo de escala.", Toast.LENGTH_SHORT).show()
            return
        }

        val score = calculateTotalScore(answers)
        Log.d("CreateScaleActivity", "Saving scale: Type=$scaleType, Score=$score, Encargado=$encargado, Fecha=$fecha")

        // 🔥 GUARDAR EN FIRESTORE
        val db = FirebaseFirestore.getInstance()
        val patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
        val patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
        val patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
        val patientAge = intent.getIntExtra("patient_age", 0)

        if (patientId == "Sin ID") {
            Toast.makeText(this, "Error: No se pudo obtener el ID del paciente.", Toast.LENGTH_SHORT).show()
            return
        }

        val patientRef = db.collection("Pacientes").document(patientId)

        // Nueva escala a guardar
        val newScale = hashMapOf(
            "tipo" to scaleType,
            "fecha" to fecha,
            "encargado" to encargado,
            "puntaje" to score
        )

        patientRef.update("escalas", FieldValue.arrayUnion(newScale))
            .addOnSuccessListener {
                Log.d("Firestore", "Escala guardada con éxito para el paciente $patientId")
                Toast.makeText(this, "Escala guardada exitosamente", Toast.LENGTH_SHORT).show()

                // Navegar a la pantalla de resultados después de guardar
                val intent = Intent(this, ScaleResultActivity::class.java).apply {
                    putExtra("scale_type", scaleType)
                    putExtra("score", score)
                    putExtra("encargado", encargado)
                    putExtra("fecha", fecha)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_gender", patientGender)
                    intent.putExtra("patient_age", patientAge)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar la escala: ${e.message}")
                Toast.makeText(this, "Error al guardar la escala: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun calculateTotalScore(answers: Map<String, String>): Int {
        var totalScore = 0
        answers.values.forEach { answer ->
            // Extrae el puntaje de la respuesta (e.g., "1 - Completamente limitado")
            val score = answer.split(" - ")[0].toIntOrNull() ?: 0
            totalScore += score
        }
        return totalScore
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