package com.example.vitalage

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

        // Acciones para el menú desplegable
        menuIcon.setOnClickListener {
            showPopupMenu(menuIcon)
        }

        // Selector de fecha (puedes integrar un DatePickerDialog aquí)
        iconCalendar.setOnClickListener {
            val datePicker = DatePickerFragment { selectedDate ->
                textFecha.text = selectedDate
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
