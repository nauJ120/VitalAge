package com.example.vitalage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
class TomaSignosActivity : AppCompatActivity() {

    private var diaSeleccionado: String? = null
    private var mesSeleccionado: String? = null
    private var anioSeleccionado: String? = null
    private lateinit var textFechaSeleccionada: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var pacienteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toma_signos)
        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        // Obtener ID del paciente dinámicamente (suponiendo que el usuario autenticado es el paciente)
        auth.currentUser?.let { user ->
            pacienteId = user.uid // Se asume que el UID de Firebase Auth es el ID del paciente
        } ?: run {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }
        // Elementos del layout
        val inputFrecuenciaRespiratoria = findViewById<EditText>(R.id.inputFrecuenciaRespiratoria)
        val inputSaturacionOxigeno = findViewById<EditText>(R.id.inputSaturacionOxigeno)
        val inputPresionArterial = findViewById<EditText>(R.id.inputPresionArterial)
        val inputTemperaturaCorporal = findViewById<EditText>(R.id.inputTemperaturaCorporal)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)

        // Botones de Fecha
        val btnDia = findViewById<Button>(R.id.btnDia)
        val btnMes = findViewById<Button>(R.id.btnMes)
        val btnAnio = findViewById<Button>(R.id.btnAnio)
        textFechaSeleccionada = findViewById(R.id.textFechaSeleccionada)

        // Selección de Día
        btnDia.setOnClickListener {
            mostrarDialogoSeleccion("Selecciona un dia", (1..31).map { it.toString() }) { seleccionado ->
                diaSeleccionado = seleccionado
                actualizarFechaSeleccionada()
            }
        }

        // Selección de Mes
        btnMes.setOnClickListener {
            val meses = listOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )
            mostrarDialogoSeleccion("Selecciona un mes", meses) { seleccionado ->
                mesSeleccionado = seleccionado
                actualizarFechaSeleccionada()
            }
        }

        // Selección de Año
        btnAnio.setOnClickListener {
            val anios = (2020..2025).map { it.toString() }
            mostrarDialogoSeleccion("Selecciona un anio", anios) { seleccionado ->
                anioSeleccionado = seleccionado
                actualizarFechaSeleccionada()
            }
        }

        // Botón Guardar con confirmación
        btnGuardar.setOnClickListener {
            val frecuencia = inputFrecuenciaRespiratoria.text.toString()
            val saturacion = inputSaturacionOxigeno.text.toString()
            val presion = inputPresionArterial.text.toString()
            val temperatura = inputTemperaturaCorporal.text.toString()

            if (frecuencia.isNotEmpty() && saturacion.isNotEmpty() && presion.isNotEmpty() && temperatura.isNotEmpty() &&
                diaSeleccionado != null && mesSeleccionado != null && anioSeleccionado != null
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Confirmacion")
                    .setMessage("Deseas guardar el registro con la fecha: ${textFechaSeleccionada.text}?")
                    .setPositiveButton("Si") { _, _ ->
                        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Toast.makeText(this, "Por favor completa todos los campos y selecciona una fecha", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cancelar
        btnCancelar.setOnClickListener {
            finish()
        }


        // Acciones para el menú desplegable
        menuIcon.setOnClickListener {
            showPopupMenu(menuIcon)
        }
    }


    private fun guardarEnFirestore(pacienteId: String, frecuencia: String, saturacion: String, presion: String, temperatura: String) {
        val registro = hashMapOf(
            "fecha" to "$diaSeleccionado de $mesSeleccionado $anioSeleccionado",
            "frecuencia_respiratoria" to frecuencia,
            "saturacion_oxigeno" to saturacion,
            "presion_arterial" to presion,
            "temperatura_corporal" to temperatura
        )

        db.collection("Pacientes").document(pacienteId)
            .collection("signos_vitales")
            .add(registro)
            .addOnSuccessListener { Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    // Función para actualizar el texto de la fecha seleccionada
    private fun actualizarFechaSeleccionada() {
        textFechaSeleccionada.text = "Fecha seleccionada: ${diaSeleccionado ?: " "} de ${mesSeleccionado ?: " "}  ${anioSeleccionado ?: " "}"
    }

    // Función para mostrar un AlertDialog con opciones
    private fun mostrarDialogoSeleccion(titulo: String, opciones: List<String>, onSelect: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setItems(opciones.toTypedArray()) { _, which ->
            onSelect(opciones[which])
        }
        builder.show()
    }

    private fun showPopupMenu(anchor: ImageView) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_toma_signos -> {
                    Toast.makeText(this, "Ya estas en Toma de Signos Vitales", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_item_historial_fecha -> {
                    val intent = Intent(this, HistorialFechaActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_historial_signos -> {
                    val intent = Intent(this, HistorialSignosActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_signos_vitales -> {
                    val intent = Intent(this, SignosVitalesActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
        }
}