package com.example.vitalage
    import android.content.Intent
    import android.os.Bundle
    import android.widget.ImageView
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

            // Navegación o acción para el menú
            menuIcon.setOnClickListener {
                // Aquí puedes abrir un menú desplegable o realizar una acción específica
                // Por ejemplo, navegar a otra actividad
                val intent = Intent(this, SignosVitalesActivity::class.java) // Reemplaza con tu actividad de menú
                startActivity(intent)
            }

            // Selector de fecha (puedes integrar un DatePickerDialog aquí)
            iconCalendar.setOnClickListener {
                // Abre un diálogo de calendario para seleccionar la fecha
                val datePicker = DatePickerFragment { selectedDate ->
                    textFecha.text = selectedDate
                }
                datePicker.show(supportFragmentManager, "datePicker")
            }
        }
    }

