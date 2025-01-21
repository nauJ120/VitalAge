package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitalage.clases.Question
import com.example.vitalage.clases.Scale


class CreateScaleActivity : AppCompatActivity() {
    private lateinit var spinnerScaleType: Spinner
    private lateinit var tvEncargado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var questionsContainer: LinearLayout

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


        // Inicializar vistas
        spinnerScaleType = findViewById(R.id.spinner_scale_type)
        tvEncargado = findViewById(R.id.tv_encargado)
        tvFecha = findViewById(R.id.tv_fecha)
        questionsContainer = findViewById(R.id.questions_container)

        // Datos automáticos
        val encargado = "Pepe Gonzalez" // Este valor debe venir del usuario actual
        val fecha = "14/01/2025" // Obtener la fecha actual dinámicamente
        tvEncargado.text = "Encargado: $encargado"
        tvFecha.text = "Fecha: $fecha"

        // Configurar Spinner
        val scaleTypes = listOf("Braden", "Barthel", "Glasgow")
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
        val btnSave: Button = findViewById(R.id.btn_save)
        btnSave.setOnClickListener {
            saveScale()
        }

    }

    private fun loadQuestions(scaleType: String) {
        questionsContainer.removeAllViews()

        // Encontrar la escala seleccionada
        val selectedScale = scales.find { it.name == scaleType } ?: return

        // Generar vistas dinámicamente
        selectedScale.questions.forEach { question ->
            // Título de la categoría
            val textView = TextView(this).apply {
                text = question.category
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            questionsContainer.addView(textView)

            // Opciones como RadioButtons
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
                if (selectedId != -1) {
                    val selectedOption = findViewById<RadioButton>(selectedId).text.toString()
                    val question = (questionsContainer.getChildAt(i - 1) as TextView).text.toString()
                    answers[question] = selectedOption
                }
            }
        }

        return answers
    }

    private fun saveScale() {
        val answers = collectAnswers()

        // Verificar si todas las preguntas tienen una respuesta
        val totalQuestions = scales.find { it.name == spinnerScaleType.selectedItem.toString() }?.questions?.size ?: 0

        if (answers.size != totalQuestions) {
            Toast.makeText(this, "Por favor responde todas las preguntas antes de continuar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Redirigir a la pantalla de resultados
        val intent = Intent(this, ScaleResultActivity::class.java).apply {
            putExtra("scale_type", spinnerScaleType.selectedItem.toString())
            putExtra("score", calculateTotalScore(answers)) // Implementa tu lógica de cálculo
        }
        startActivity(intent)
        finish()
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


}