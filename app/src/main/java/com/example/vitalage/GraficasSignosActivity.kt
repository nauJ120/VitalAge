package com.example.vitalage

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitalage.databinding.ActivityGraficasSignosBinding
import com.example.vitalage.databinding.ActivityInformePacienteBinding
import com.example.vitalage.model.SignosVitales
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GraficasSignosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var listaSignos: List<SignosVitales>
    private lateinit var patientId: String
    private lateinit var patientName: String
    private var usuarioActual: String = "Desconocido"
    private lateinit var binding: ActivityGraficasSignosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graficas_signos)
        binding = ActivityGraficasSignosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Conexión a Firestore y carga de datos
        db = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        obtenerNombreUsuario { nombre ->
            usuarioActual = nombre
            binding.tvSubtitulo.text = "Usuario: $usuarioActual"
        }

        findViewById<ImageView>(com.example.vitalage.R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(com.example.vitalage.R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(com.example.vitalage.R.id.btnProfileContainer).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }



        // Recibe datos
        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: "Paciente"
        obtenerSignosDesdeFirestore()

        // Setear listeners para los botones de rango
        setListenersParaBotones()
    }

    // Función para obtener los datos desde Firestore
    private fun obtenerSignosDesdeFirestore() {
        // Usa el patientId que se pasa a través del Intent
        val pacienteId = patientId

        db.collection("Pacientes").document(pacienteId)  // Consulta Firestore usando el patientId
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("signos_vitales")) {
                    val lista = document["signos_vitales"] as List<Map<String, Any>>
                    listaSignos = lista.mapNotNull { map ->
                        try {
                            SignosVitales(
                                fecha = map["fecha"] as String,
                                frecuenciaCardiaca = (map["frecuencia_cardiaca"] as Long).toInt(),
                                frecuenciaRespiratoria = (map["frecuencia_respiratoria"] as Long).toInt(),
                                saturacionOxigeno = (map["saturacion_oxigeno"] as Long).toInt(),
                                presionArterial = (map["presion_arterial"] as Number).toInt(),
                                temperatura = (map["temperatura"] as Number).toDouble(),
                                peso = (map["peso"] as Number).toDouble(),
                                imc = (map["imc"] as Number).toDouble(),
                                encargado = map["encargado"] as String,
                                escalaDolor = (map["escala_dolor"] as Long).toInt()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    // Al cargar los datos, se actualizan las gráficas con un rango por defecto (7 días)
                    cargarTodasLasGraficas(listaSignos, dias = 7)
                }
            }
    }



    // Función para cargar las gráficas
    private fun cargarTodasLasGraficas(lista: List<SignosVitales>, dias: Long) {
        val datosFiltrados = filtrarPorDias(lista, dias)

        graficar(datosFiltrados, R.id.chartFrecuenciaCardiaca) { it.frecuenciaCardiaca.toFloat() }
        graficar(datosFiltrados, R.id.chartFrecuenciaRespiratoria) { it.frecuenciaRespiratoria.toFloat() }
        graficar(datosFiltrados, R.id.chartTemperatura) { it.temperatura.toFloat() }
        graficar(datosFiltrados, R.id.chartPresionArterial) { it.presionArterial.toFloat() }
        graficar(datosFiltrados, R.id.chartSaturacionOxigeno) { it.saturacionOxigeno.toFloat() }
        graficar(datosFiltrados, R.id.chartPeso) { it.peso.toFloat() }
        graficar(datosFiltrados, R.id.chartIMC) { it.imc.toFloat() }
        graficar(datosFiltrados, R.id.chartEscalaDolor) { it.escalaDolor.toFloat() }

        // Aquí debes configurar los botones de rango
        setListenersParaBotones()
    }

    // Función genérica para graficar datos
    private fun graficar(
        lista: List<SignosVitales>,
        chartId: Int,
        selector: (SignosVitales) -> Float
    ) {
        val entries = lista.mapIndexed { index, it ->
            // Usamos la fecha como el valor del eje X
            val fecha = LocalDateTime.parse(it.fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val x = ChronoUnit.DAYS.between(fecha, LocalDateTime.now()).toFloat() // Días transcurridos desde la fecha
            Entry(x, selector(it)) // El valor de Y se obtiene usando el selector (frecuencia, temperatura, etc.)
        }

        val dataSet = LineDataSet(entries, "").apply {
            setDrawFilled(true)
            fillColor = Color.parseColor("#BBDEFB")
            color = Color.parseColor("#1E88E5")
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.LINEAR
        }

        val lineData = LineData(dataSet)
        val chart = findViewById<LineChart>(chartId)
        chart.data = lineData
        chart.description.isEnabled = false
        chart.invalidate()
    }


    // Función para filtrar los datos por rango de tiempo (en días)
    private fun filtrarPorDias(lista: List<SignosVitales>, dias: Long): List<SignosVitales> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val ahora = LocalDateTime.now()

        return lista.filter {
            try {
                val fecha = LocalDateTime.parse(it.fecha, formatter)
                val diasTranscurridos = ChronoUnit.DAYS.between(fecha, ahora)
                Log.d("FiltrarPorDias", "Fecha: ${it.fecha}, Días transcurridos: $diasTranscurridos, Rango: $dias")
                diasTranscurridos <= dias
            } catch (e: Exception) {
                Log.e("FiltrarPorDias", "Error al procesar la fecha: ${it.fecha}", e)
                false
            }
        }
    }


    // Función para establecer los listeners para los botones de rango
    private fun setListenersParaBotones() {
        // Frecuencia Cardíaca
        findViewById<Button>(R.id.btnFC1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartFrecuenciaCardiaca) { it.frecuenciaCardiaca.toFloat() }
        }
        findViewById<Button>(R.id.btnFC5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartFrecuenciaCardiaca) { it.frecuenciaCardiaca.toFloat() }
        }
        findViewById<Button>(R.id.btnFC1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartFrecuenciaCardiaca) { it.frecuenciaCardiaca.toFloat() }
        }
        findViewById<Button>(R.id.btnFC1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartFrecuenciaCardiaca) { it.frecuenciaCardiaca.toFloat() }
        }

        // Frecuencia Respiratoria
        findViewById<Button>(R.id.btnFR1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartFrecuenciaRespiratoria) { it.frecuenciaRespiratoria.toFloat() }
        }
        findViewById<Button>(R.id.btnFR5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartFrecuenciaRespiratoria) { it.frecuenciaRespiratoria.toFloat() }
        }
        findViewById<Button>(R.id.btnFR1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartFrecuenciaRespiratoria) { it.frecuenciaRespiratoria.toFloat() }
        }
        findViewById<Button>(R.id.btnFR1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartFrecuenciaRespiratoria) { it.frecuenciaRespiratoria.toFloat() }
        }

        // Temperatura
        findViewById<Button>(R.id.btnTemp1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartTemperatura) { it.temperatura.toFloat() }
        }
        findViewById<Button>(R.id.btnTemp5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartTemperatura) { it.temperatura.toFloat() }
        }
        findViewById<Button>(R.id.btnTemp1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartTemperatura) { it.temperatura.toFloat() }
        }
        findViewById<Button>(R.id.btnTemp1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartTemperatura) { it.temperatura.toFloat() }
        }

        // Presión Arterial
        findViewById<Button>(R.id.btnPA1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartPresionArterial) { it.presionArterial.toFloat() }
        }
        findViewById<Button>(R.id.btnPA5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartPresionArterial) { it.presionArterial.toFloat() }
        }
        findViewById<Button>(R.id.btnPA1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartPresionArterial) { it.presionArterial.toFloat() }
        }
        findViewById<Button>(R.id.btnPA1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartPresionArterial) { it.presionArterial.toFloat() }
        }

        // IMC
        findViewById<Button>(R.id.btnIMC1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartIMC) { it.imc.toFloat() }
        }
        findViewById<Button>(R.id.btnIMC5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartIMC) { it.imc.toFloat() }
        }
        findViewById<Button>(R.id.btnIMC1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartIMC) { it.imc.toFloat() }
        }
        findViewById<Button>(R.id.btnIMC1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartIMC) { it.imc.toFloat() }
        }

        // Peso
        findViewById<Button>(R.id.btnPeso1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartPeso) { it.peso.toFloat() }
        }
        findViewById<Button>(R.id.btnPeso5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartPeso) { it.peso.toFloat() }
        }
        findViewById<Button>(R.id.btnPeso1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartPeso) { it.peso.toFloat() }
        }
        findViewById<Button>(R.id.btnPeso1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartPeso) { it.peso.toFloat() }
        }

        // Saturación de Oxígeno
        findViewById<Button>(R.id.btnSO21D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartSaturacionOxigeno) { it.saturacionOxigeno.toFloat() }
        }
        findViewById<Button>(R.id.btnSO25D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartSaturacionOxigeno) { it.saturacionOxigeno.toFloat() }
        }
        findViewById<Button>(R.id.btnSO21M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartSaturacionOxigeno) { it.saturacionOxigeno.toFloat() }
        }
        findViewById<Button>(R.id.btnSO21A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartSaturacionOxigeno) { it.saturacionOxigeno.toFloat() }
        }

        // Escala de Dolor
        findViewById<Button>(R.id.btnEscalaDolor1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartEscalaDolor) { it.escalaDolor.toFloat() }
        }
        findViewById<Button>(R.id.btnEscalaDolor5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartEscalaDolor) { it.escalaDolor.toFloat() }
        }
        findViewById<Button>(R.id.btnEscalaDolor1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartEscalaDolor) { it.escalaDolor.toFloat() }
        }
        findViewById<Button>(R.id.btnEscalaDolor1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartEscalaDolor) { it.escalaDolor.toFloat() }
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
