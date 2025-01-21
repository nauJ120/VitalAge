package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalage.clases.DateFilter
import com.example.vitalage.adapters.ScaleAdapter
import com.example.vitalage.clases.Escala
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaEscalasActivity : AppCompatActivity() {

    private lateinit var rvScales: RecyclerView
    private lateinit var adapter: ScaleAdapter

    private lateinit var tvEmptyListMessage: TextView

    private lateinit var btnFilters: Button
    private lateinit var filterMenu: LinearLayout
    private lateinit var btnApplyFilters: Button
    private lateinit var btnResetFilters: Button

    private lateinit var chipGroupType: ChipGroup
    private lateinit var chipGroupNurse: ChipGroup

    private lateinit var etDay: EditText
    private lateinit var etMonth: EditText
    private lateinit var etYear: EditText

    private val encargadosList = listOf("Pepe Gonzalez", "Maria Perez", "Juanita Lopez", "Amongus")
    private val escalasList = listOf("Braden", "Barthel", "Glasgow")
    private val allEscalas = listOf(
        Escala("Braden", "14/01/2025", "Pepe Gonzalez", 85),
        Escala("Barthel", "12/01/2025", "Maria Perez", 90),
        Escala("Glasgow", "10/01/2025", "Juanita Lopez", 70),
        Escala("Barthel", "15/01/2025", "Pepe Gonzalez", 95)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_escalas)

        rvScales = findViewById(R.id.rv_scales)
        rvScales.layoutManager = LinearLayoutManager(this)

        tvEmptyListMessage = findViewById(R.id.tv_empty_list_message)

        btnFilters = findViewById(R.id.btn_filters)
        filterMenu = findViewById(R.id.filter_menu)
        btnApplyFilters = findViewById(R.id.btn_apply_filters)
        btnResetFilters = findViewById(R.id.btn_reset_filters)

        chipGroupType = findViewById(R.id.chip_group_type)
        chipGroupNurse = findViewById(R.id.chip_group_nurse)

        etDay = findViewById(R.id.et_day)
        etMonth = findViewById(R.id.et_month)
        etYear = findViewById(R.id.et_year)


        adapter = ScaleAdapter(allEscalas)
        rvScales.adapter = adapter

        setupEscalasFilter()
        setupEncargadosFilter()

        btnFilters.setOnClickListener {
            filterMenu.visibility = if (filterMenu.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        btnApplyFilters.setOnClickListener {
            val selectedTypes = getSelectedChips(chipGroupType)
            val selectedNurses = getSelectedChips(chipGroupNurse)
            applyFilters(selectedTypes, selectedNurses)
            filterMenu.visibility = View.GONE
        }

        btnResetFilters.setOnClickListener {
            chipGroupType.removeAllViews()
            chipGroupNurse.removeAllViews()
            etDay.text.clear()
            etMonth.text.clear()
            etYear.text.clear()

            setupEscalasFilter()
            setupEncargadosFilter()
            updateRecyclerView(allEscalas)
            btnFilters.text = "Filtros (0)"
            filterMenu.visibility = View.GONE
        }

        val btnCrearPantalla: FloatingActionButton = findViewById(R.id.fab_add)
        btnCrearPantalla.setOnClickListener {
            val intent = Intent(this, CreateScaleActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupEscalasFilter() {
        escalasList.forEach { escala ->
            val chip = Chip(this).apply {
                text = escala
                isCheckable = true
            }
            chipGroupType.addView(chip)
        }
    }

    private fun setupEncargadosFilter() {
        encargadosList.forEach { encargado ->
            val chip = Chip(this).apply {
                text = encargado
                isCheckable = true
            }
            chipGroupNurse.addView(chip)
        }
    }

    private fun getSelectedChips(chipGroup: ChipGroup): List<String> {
        val selectedChips = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedChips.add(chip.text.toString())
            }
        }
        return selectedChips
    }

    private fun getSelectedDate(): DateFilter {
        val day = etDay.text.toString().takeIf { it.isNotEmpty() }  // Tomar solo si no está vacío
        val month = etMonth.text.toString().takeIf { it.isNotEmpty() }
        val year = etYear.text.toString().takeIf { it.isNotEmpty() }

        return DateFilter(day, month, year)  // Retornar un objeto con las partes disponibles
    }



    private fun applyFilters(selectedTypes: List<String>, selectedNurses: List<String>) {
        val selectedDate = getSelectedDate()  // Obtener fecha parcial
        var filteredList = allEscalas

        // Filtro por tipo
        if (selectedTypes.isNotEmpty()) {
            filteredList = filteredList.filter { it.tipo in selectedTypes }
        }

        // Filtro por encargado
        if (selectedNurses.isNotEmpty()) {
            filteredList = filteredList.filter { it.encargado in selectedNurses }
        }

        // Filtro por fecha parcial
        filteredList = filteredList.filter { escala ->
            (selectedDate.day == null || escala.fecha.split("/")[0] == selectedDate.day) &&
                    (selectedDate.month == null || escala.fecha.split("/")[1] == selectedDate.month) &&
                    (selectedDate.year == null || escala.fecha.split("/")[2] == selectedDate.year)
        }

        // Actualizar RecyclerView
        updateRecyclerView(filteredList)

        // Actualizar contador de filtros activos
        val activeFilters = selectedTypes.size + selectedNurses.size +
                listOfNotNull(selectedDate.day, selectedDate.month, selectedDate.year).size
        btnFilters.text = "Filtros ($activeFilters)"
    }


    private fun updateRecyclerView(filteredList: List<Escala>) {
        if (filteredList.isEmpty()) {
            rvScales.visibility = View.GONE
            tvEmptyListMessage.visibility = View.VISIBLE
        } else {
            rvScales.visibility = View.VISIBLE
            tvEmptyListMessage.visibility = View.GONE
        }

        adapter = ScaleAdapter(filteredList)
        rvScales.adapter = adapter
    }
}
