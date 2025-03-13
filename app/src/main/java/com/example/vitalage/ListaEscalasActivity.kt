    package com.example.vitalage

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.LinearLayout
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.vitalage.clases.DateFilter
    import com.example.vitalage.adapters.ScaleAdapter
    import com.example.vitalage.clases.Escala
    import com.example.vitalage.databinding.ActivityListaEscalasBinding
    import com.example.vitalage.model.SpaceItemDecoration
    import com.google.android.material.chip.Chip
    import com.google.android.material.chip.ChipGroup
    import com.google.android.material.floatingactionbutton.FloatingActionButton
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener
    import com.google.firebase.firestore.FirebaseFirestore

    class ListaEscalasActivity : AppCompatActivity() {

        private lateinit var binding: ActivityListaEscalasBinding

        private lateinit var patientName: String
        private lateinit var patientId: String
        private lateinit var patientGender: String
        private var patientAge: Int = 0

        private lateinit var adapter: ScaleAdapter
        private lateinit var recyclerView: RecyclerView

        private val encargadosList = mutableListOf<String>() // 🔥 Se inicializa vacía
        private val escalasList = listOf("Braden", "Barthel", "Glasgow")
        private val allEscalas = mutableListOf<Escala>()

        private var usuarioActual: String = "Desconocido"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)



            // 🔥 Inicializar Binding correctamente
            binding = ActivityListaEscalasBinding.inflate(layoutInflater)
            setContentView(binding.root)  // 🔥 Esto es fundamental para usar View Binding

            obtenerNombreUsuario { nombre ->
                usuarioActual = nombre
                binding.tvUser.text = "Usuario: $usuarioActual"
            }

            findViewById<LinearLayout>(R.id.btnHomeContainer).setOnClickListener {
                startActivity(Intent(this, PatientListActivity::class.java))
            }

            findViewById<LinearLayout>(R.id.btnProfileContainer).setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))}

            // Recibir datos del Intent
            patientName = intent.getStringExtra("patient_name") ?: "Desconocido"
            patientId = intent.getStringExtra("patient_id") ?: "Sin ID"
            patientGender = intent.getStringExtra("patient_gender") ?: "No especificado"
            patientAge = intent.getIntExtra("patient_age", 0)

            // Mostrar datos del paciente
            binding.tvResidentName.text = patientName
            binding.tvResidentInfo.text = "ID: $patientId • Sexo: $patientGender • Edad: $patientAge años"

            // 🔥 Configurar RecyclerView correctamente
            binding.rvScales.layoutManager = LinearLayoutManager(this)
            adapter = ScaleAdapter(allEscalas.toMutableList()) // 🔥 Convertimos a lista mutable
            binding.rvScales.adapter = adapter

            setupEscalasFilter()
            setupEncargadosFilter()

            binding.btnFilters.setOnClickListener {
                binding.filterMenu.visibility = if (binding.filterMenu.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            binding.btnApplyFilters.setOnClickListener {
                val selectedTypes = getSelectedChips(binding.chipGroupType)
                val selectedNurses = getSelectedChips(binding.chipGroupNurse)
                applyFilters(selectedTypes, selectedNurses)
                binding.filterMenu.visibility = View.GONE
            }

            binding.btnResetFilters.setOnClickListener {
                binding.chipGroupType.removeAllViews()
                binding.chipGroupNurse.removeAllViews()
                binding.etDay.text.clear()
                binding.etMonth.text.clear()
                binding.etYear.text.clear()

                setupEscalasFilter()
                setupEncargadosFilter()
                updateRecyclerView(allEscalas)
                binding.btnFilters.text = "Filtros (0)"
                binding.filterMenu.visibility = View.GONE
            }

            binding.fabAdd.setOnClickListener {
                val intent = Intent(this, CreateScaleActivity::class.java)
                intent.putExtra("patient_name", patientName)
                intent.putExtra("patient_id", patientId)
                intent.putExtra("patient_gender", patientGender)
                intent.putExtra("patient_age", patientAge)
                startActivity(intent)
            }

            binding.rvScales.addItemDecoration(SpaceItemDecoration(16))

            fetchEncargadosFromRealtimeDatabase()
            fetchScalesFromFirestore()
        }


        private fun setupEscalasFilter() {
            escalasList.forEach { escala ->
                val chip = Chip(this).apply {
                    text = escala
                    isCheckable = true
                }
                binding.chipGroupType.addView(chip)
            }
        }

        private fun setupEncargadosFilter() {
            encargadosList.forEach { encargado ->
                val chip = Chip(this).apply {
                    text = encargado
                    isCheckable = true
                }
                binding.chipGroupNurse.addView(chip)
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
            val day = binding.etDay.text.toString().takeIf { it.isNotEmpty() }  // Tomar solo si no está vacío
            val month = binding.etMonth.text.toString().takeIf { it.isNotEmpty() }
            val year = binding.etYear.text.toString().takeIf { it.isNotEmpty() }

            return DateFilter(day, month, year)  // Retornar un objeto con las partes disponibles
        }



        private fun applyFilters(selectedTypes: List<String>, selectedNurses: List<String>) {
            val selectedDate = getSelectedDate()  // Obtener fecha parcial

            // 🔥 Clonamos la lista original para evitar modificar `allEscalas`
            var filteredList = allEscalas.toList()

            // 🔹 Filtro por tipo de escala
            if (selectedTypes.isNotEmpty()) {
                filteredList = filteredList.filter { it.tipo in selectedTypes }
            }

            // 🔹 Filtro por encargado
            if (selectedNurses.isNotEmpty()) {
                filteredList = filteredList.filter { it.encargado in selectedNurses }
            }

            // 🔹 Filtro por fecha (parcial)
            filteredList = filteredList.filter { escala ->
                val fechaParts = escala.fecha.split("/") // Intenta dividir la fecha
                if (fechaParts.size == 3) {
                    val (dia, mes, año) = fechaParts
                    (selectedDate.day == null || dia == selectedDate.day) &&
                            (selectedDate.month == null || mes == selectedDate.month) &&
                            (selectedDate.year == null || año == selectedDate.year)
                } else {
                    false // Si la fecha no tiene el formato esperado, se ignora el elemento
                }
            }

            // 🔹 Actualizar RecyclerView con la lista filtrada
            updateRecyclerView(filteredList)

            // 🔹 Contador de filtros activos
            val activeFilters = selectedTypes.size + selectedNurses.size +
                    listOfNotNull(selectedDate.day, selectedDate.month, selectedDate.year).size
            binding.btnFilters.text = "Filtros ($activeFilters)"
        }



        private fun updateRecyclerView(filteredList: List<Escala>) {
            if (filteredList.isEmpty()) {
                binding.rvScales.visibility = View.GONE
                binding.tvEmptyListMessage.visibility = View.VISIBLE
            } else {
                binding.rvScales.visibility = View.VISIBLE
                binding.tvEmptyListMessage.visibility = View.GONE
            }

            adapter.updateData(filteredList.toMutableList())
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

        private fun fetchScalesFromFirestore() {
            val db = FirebaseFirestore.getInstance()
            val patientRef = db.collection("Pacientes").document(patientId)

            patientRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val scales = document.get("escalas") as? List<Map<String, Any>> ?: emptyList()

                    allEscalas.clear() // 🔥 Limpiar la lista antes de agregar nuevas escalas

                    scales.forEach { scale ->
                        val tipo = scale["tipo"] as? String ?: "Desconocido"
                        val fecha = scale["fecha"] as? String ?: "Sin Fecha"
                        val encargadoNombre = scale["encargado"] as? String ?: "No Asignado"
                        val puntaje = (scale["puntaje"] as? Number)?.toInt() ?: 0

                        allEscalas.add(Escala(tipo, fecha, encargadoNombre, puntaje))
                    }

                    // 🔥 En lugar de solo `notifyDataSetChanged()`, actualizamos directamente la RecyclerView
                    updateRecyclerView(allEscalas)

                } else {
                    Toast.makeText(this, "No se encontraron escalas para este paciente", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener escalas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }





        private fun fetchEncargadosFromRealtimeDatabase() {
            val databaseRef = FirebaseDatabase.getInstance().getReference("user").child("users")

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    encargadosList.clear() // 🔥 Limpiar lista antes de llenarla nuevamente

                    for (userSnapshot in snapshot.children) {
                        val nombreUsuario = userSnapshot.child("nombre").value as? String
                            ?: userSnapshot.child("nombre_usuario").value as? String
                            ?: "Desconocido"

                        encargadosList.add(nombreUsuario) // Agregar el nombre a la lista
                    }

                    // 🔥 Una vez que tenemos la lista, actualizamos los filtros en la UI
                    setupEncargadosFilter()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al obtener encargados: ${error.message}")
                }
            })
        }




    }
