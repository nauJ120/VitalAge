package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Opciones del menú
        val menuOptions = listOf(
            MenuOption("Notas de enfermería", R.drawable.ic_nursing_notes),
            MenuOption("Signos vitales", R.drawable.ic_vital_signs),
            MenuOption("Escalas", R.drawable.ic_scales),
            MenuOption("Inventario y Control", R.drawable.ic_inventory),
            MenuOption("Terapias", R.drawable.ic_therapies),
            MenuOption("Informes", R.drawable.ic_reports),
            MenuOption("Cámara", R.drawable.ic_camera)
        )

        // Configurar el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_menu_options)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MenuAdapter(menuOptions) { menuOption ->
            // Acción al hacer clic en una opción del menú
            when (menuOption.title) {
                "Notas de enfermería" -> {
                    val intent = Intent(this, NursingNotesActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    // Puedes agregar más acciones aquí para otras opciones del menú
                }
            }
        }
    }
}
