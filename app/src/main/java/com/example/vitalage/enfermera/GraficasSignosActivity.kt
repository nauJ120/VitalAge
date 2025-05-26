package com.example.vitalage.enfermera

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalage.generales.ProfileActivity
import com.example.vitalage.R
import com.example.vitalage.databinding.ActivityGraficasSignosBinding
import com.example.vitalage.model.SignosVitales
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
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
        val pacienteId = patientId

        db.collection("Pacientes").document(pacienteId)
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
                    Log.d("GraficasSignos", "Datos obtenidos: ${listaSignos.size} signos vitales encontrados")
                    cargarTodasLasGraficas(listaSignos, dias = 7)
                }
            }
    }

    // Función para cargar las gráficas
    private fun cargarTodasLasGraficas(lista: List<SignosVitales>, dias: Long) {
        // Filtra los datos según el rango de días
        val datosFiltrados = filtrarPorDias(lista, dias)

        // Ahora pasamos el rango adecuado en cada llamada de graficar
        graficar(datosFiltrados,
            R.id.chartFrecuenciaCardiaca,  { it.frecuenciaCardiaca.toFloat() }, 5L)
        graficar(datosFiltrados,
            R.id.chartFrecuenciaRespiratoria,  { it.frecuenciaRespiratoria.toFloat() }, 5L)
        graficar(datosFiltrados, R.id.chartTemperatura,  { it.temperatura.toFloat() }, 5L)
        graficar(datosFiltrados, R.id.chartPresionArterial,  { it.presionArterial.toFloat() }, 5L)
        graficar(datosFiltrados,
            R.id.chartSaturacionOxigeno,  { it.saturacionOxigeno.toFloat() }, 5L)
        graficar(datosFiltrados, R.id.chartPeso,  { it.peso.toFloat() }, 5L)
        graficar(datosFiltrados, R.id.chartIMC,  { it.imc.toFloat() }, 5L)
        graficar(datosFiltrados, R.id.chartEscalaDolor,  { it.escalaDolor.toFloat() }, 5L)

        // Configura los botones de rango después de graficar
        setListenersParaBotones()
    }



// Función para graficar los datos
private fun graficar(
    lista: List<SignosVitales>,
    chartId: Int,
    selector: (SignosVitales) -> Float,
    rango: Long
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val now = LocalDateTime.now()

    val entries = lista.mapNotNull {
        try {
            val fecha = LocalDateTime.parse(it.fecha, formatter)
            val x = when (rango) {
                1L -> {
                    if (fecha.toLocalDate() == now.toLocalDate()) {
                        fecha.hour + (fecha.minute / 60.0)
                    } else {
                        return@mapNotNull null
                    }
                }
                5L -> ChronoUnit.DAYS.between(fecha.toLocalDate(), now.toLocalDate()).toDouble()
                30L -> fecha.dayOfMonth.toDouble()
                365L -> ((fecha.monthValue - 1) / 2.0)
                else -> 0.0
            }.toFloat()


            val y = selector(it)

            if (x.isNaN() || y.isNaN() || x < 0 || y < 0) null else Entry(x, y)
        } catch (e: Exception) {
            Log.e("Graficar", "Error al generar entrada para gráfica: ${e.message}")
            null
        }
    }

    val validEntries = entries.filter { !it.x.isNaN() && !it.y.isNaN() && it.x >= 0 && it.y >= 0 }
    val chart = findViewById<LineChart>(chartId)

    if (validEntries.isEmpty()) {
        chart.clear()
        chart.setNoDataText("No hay datos válidos para graficar.")
        return
    }

    val dataSet = LineDataSet(validEntries, "").apply {
        setDrawFilled(true)
        fillColor = Color.parseColor("#BBDEFB")
        color = Color.parseColor("#1E88E5")
        setDrawCircles(true)
        lineWidth = 2f
        mode = LineDataSet.Mode.LINEAR
        setDrawValues(true)
        valueTextSize = 10f
    }

    chart.data = LineData(dataSet)

    chart.setTouchEnabled(false)
    chart.setDragEnabled(false)
    chart.setScaleEnabled(false)
    chart.setPinchZoom(false)
    chart.setDoubleTapToZoomEnabled(false)

    chart.axisLeft.apply {
        axisMinimum = 0f
        axisMaximum = when (chartId) {
            R.id.chartTemperatura -> 45f
            R.id.chartFrecuenciaCardiaca, R.id.chartFrecuenciaRespiratoria -> 200f
            R.id.chartPresionArterial -> 200f
            R.id.chartIMC -> 60f
            R.id.chartPeso -> 200f
            R.id.chartSaturacionOxigeno -> 100f
            R.id.chartEscalaDolor -> 10f
            else -> 100f
        }
        labelCount = 6
        granularity = 1f
    }

    chart.axisRight.isEnabled = false
    chart.description.isEnabled = false

    configurarEjeX(chart.xAxis, rango, now)

    try {
        chart.invalidate()
    } catch (e: Exception) {
        Log.e("Graficar", "Error al renderizar gráfico: ${e.message}")
        chart.clear()
        chart.setNoDataText("No se pudo graficar por datos inválidos.")
    }
}



    private fun configurarEjeX(xAxis: XAxis, rango: Long, now: LocalDateTime) {
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -30f
        xAxis.setLabelCount(5, true)

        xAxis.valueFormatter = when (rango) {
            1L -> object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val hour = value.toInt()
                    val minute = ((value - hour) * 60).toInt()
                    return String.format("%02d:%02d", hour, minute)
                }
            }
            5L -> object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return now.minusDays(value.toLong()).format(DateTimeFormatter.ofPattern("d MMM"))
                }
            }
            30L -> object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "Día ${value.toInt()}"
                }
            }

            365L -> IndexAxisValueFormatter(listOf("Ene-Feb", "Mar-Abr", "May-Jun", "Jul-Ago", "Sep-Oct", "Nov-Dic"))
            else -> IndexAxisValueFormatter()
        }
    }








    // Función para filtrar los datos por rango de tiempo (en días)
    private fun filtrarPorDias(lista: List<SignosVitales>, dias: Long): List<SignosVitales> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val ahora = LocalDateTime.now()

        // Filtramos los datos según los días
        val datosFiltrados = lista.filter {
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

        // Verifica cuántos datos se están filtrando
        Log.d("FiltrarPorDias", "Datos filtrados: ${datosFiltrados.size}")
        return if (datosFiltrados.isEmpty()) lista else datosFiltrados
    }




    // Función para establecer los listeners para los botones de rango
    private fun setListenersParaBotones() {
        // Frecuencia Cardíaca
        findViewById<Button>(R.id.btnFC1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            Log.d("Boton1D", "Datos filtrados: ${filtrados.size} elementos")
            graficar(filtrados,
                R.id.chartFrecuenciaCardiaca, { it.frecuenciaCardiaca.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnFC5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            Log.d("Boton5D", "Datos filtrados: ${filtrados.size} elementos")
            graficar(filtrados,
                R.id.chartFrecuenciaCardiaca, { it.frecuenciaCardiaca.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnFC1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            Log.d("Boton1M", "Datos filtrados: ${filtrados.size} elementos")
            graficar(filtrados,
                R.id.chartFrecuenciaCardiaca, { it.frecuenciaCardiaca.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnFC1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            Log.d("Boton1A", "Datos filtrados: ${filtrados.size} elementos")
            graficar(filtrados,
                R.id.chartFrecuenciaCardiaca, { it.frecuenciaCardiaca.toFloat() }, 365L)
        }

        // Frecuencia Respiratoria
        findViewById<Button>(R.id.btnFR1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados,
                R.id.chartFrecuenciaRespiratoria, { it.frecuenciaRespiratoria.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnFR5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados,
                R.id.chartFrecuenciaRespiratoria, { it.frecuenciaRespiratoria.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnFR1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados,
                R.id.chartFrecuenciaRespiratoria, { it.frecuenciaRespiratoria.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnFR1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados,
                R.id.chartFrecuenciaRespiratoria, { it.frecuenciaRespiratoria.toFloat() }, 365L)
        }

        // Temperatura
        findViewById<Button>(R.id.btnTemp1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartTemperatura, { it.temperatura.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnTemp5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartTemperatura, { it.temperatura.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnTemp1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartTemperatura, { it.temperatura.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnTemp1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartTemperatura, { it.temperatura.toFloat() }, 365L)
        }

        // Presión Arterial
        findViewById<Button>(R.id.btnPA1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartPresionArterial, { it.presionArterial.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnPA5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartPresionArterial, { it.presionArterial.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnPA1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartPresionArterial, { it.presionArterial.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnPA1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartPresionArterial, { it.presionArterial.toFloat() }, 365L)
        }

        // IMC
        findViewById<Button>(R.id.btnIMC1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartIMC, { it.imc.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnIMC5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartIMC, { it.imc.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnIMC1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartIMC, { it.imc.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnIMC1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartIMC, { it.imc.toFloat() }, 365L)
        }

        // Peso
        findViewById<Button>(R.id.btnPeso1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartPeso, { it.peso.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnPeso5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartPeso, { it.peso.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnPeso1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartPeso, { it.peso.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnPeso1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartPeso, { it.peso.toFloat() }, 365L)
        }

        // Saturación de Oxígeno
        findViewById<Button>(R.id.btnSO21D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartSaturacionOxigeno, { it.saturacionOxigeno.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnSO25D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartSaturacionOxigeno, { it.saturacionOxigeno.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnSO21M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartSaturacionOxigeno, { it.saturacionOxigeno.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnSO21A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados,
                R.id.chartSaturacionOxigeno, { it.saturacionOxigeno.toFloat() }, 365L)
        }

        // Escala de Dolor
        findViewById<Button>(R.id.btnEscalaDolor1D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 1)
            graficar(filtrados, R.id.chartEscalaDolor, { it.escalaDolor.toFloat() }, 1L)
        }

        findViewById<Button>(R.id.btnEscalaDolor5D).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 5)
            graficar(filtrados, R.id.chartEscalaDolor, { it.escalaDolor.toFloat() }, 5L)
        }

        findViewById<Button>(R.id.btnEscalaDolor1M).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 30)
            graficar(filtrados, R.id.chartEscalaDolor, { it.escalaDolor.toFloat() }, 30L)
        }

        findViewById<Button>(R.id.btnEscalaDolor1A).setOnClickListener {
            val filtrados = filtrarPorDias(listaSignos, 365)
            graficar(filtrados, R.id.chartEscalaDolor, { it.escalaDolor.toFloat() }, 365L)
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
