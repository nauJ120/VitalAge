package com.example.vitalage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistorialFechaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signos_vitales_historial_fecha)

        // Elementos del layout
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        val iconCalendar = findViewById<ImageView>(R.id.iconCalendar)
        val textFecha = findViewById<TextView>(R.id.textFecha)

        // Base de datos simulada (esto debe ser reemplazado con una real en el futuro)
        val registrosMock = mapOf(
            "28/01/2025" to true,  // Fecha con registro
            "29/01/2025" to false  // Fecha sin registro
        )

        // Acciones para el menú desplegable
        menuIcon.setOnClickListener {
            showPopupMenu(menuIcon)
        }

        // Selector de fecha con verificación en la base de datos simulada
        iconCalendar.setOnClickListener {
            val datePicker = DatePickerFragment { selectedDate ->
                textFecha.text = selectedDate

                // Verificar si la fecha tiene registros en la base de datos simulada
                val existeRegistro = registrosMock[selectedDate] ?: false

                if (existeRegistro) {
                    // Si hay un registro, ir a la pantalla de historial con los detalles
                    val intent = Intent(this, HistorialSignosActivity::class.java)
                    intent.putExtra("fechaSeleccionada", selectedDate) // Pasar la fecha
                    startActivity(intent)
                } else {
                    // Si no hay registro, mostrar un mensaje de alerta
                    AlertDialog.Builder(this)
                        .setTitle("Sin registros")
                        .setMessage("No se encontraron registros para la fecha seleccionada.")
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
            }
            datePicker.show(supportFragmentManager, "datePicker")
        }
    }

    private fun showPopupMenu(anchor: ImageView) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_toma_signos -> {
                    val intent = Intent(this, TomaSignosActivity::class.java)
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
