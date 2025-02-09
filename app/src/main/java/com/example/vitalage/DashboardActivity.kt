package com.example.vitalage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.example.vitalage.models.Paciente
import com.example.vitalage.models.SignoVital
import com.example.vitalage.models.Medicamento


class DashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var signosVitalesAdapter: HistorialSignosActivity
    private lateinit var medicamentosAdapter: MedicamentosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setSupportActionBar(findViewById(R.id.toolbar))

        db = FirebaseFirestore.getInstance()

        val rvSignosVitales = findViewById<RecyclerView>(R.id.rvSignosVitales)
        val rvMedicamentos = findViewById<RecyclerView>(R.id.rvMedicamentos)
        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarSignos)

        rvSignosVitales.layoutManager = LinearLayoutManager(this)
        rvMedicamentos.layoutManager = LinearLayoutManager(this)

        signosVitalesAdapter = HistorialSignosActivity()
        medicamentosAdapter = MedicamentosAdapter()

        rvSignosVitales.adapter = signosVitalesAdapter
        rvMedicamentos.adapter = medicamentosAdapter

        cargarSignosVitales()
        cargarMedicamentos()

        fabAgregar.setOnClickListener {
            agregarSignosVitales()
        }
    }

    private fun cargarSignosVitales() {
        db.collection("historial_signos").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.toObjects(SignoVital::class.java) ?: emptyList()
            signosVitalesAdapter.actualizarLista(lista)
        }
    }

    private fun cargarMedicamentos() {
        db.collection("medicamentos").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.toObjects(Medicamento::class.java) ?: emptyList()
            medicamentosAdapter.actualizarLista(lista)
        }
    }

    private fun agregarSignosVitales() {
        val nuevoRegistro = SignoVital("2025-02-09", "120/80", "75")
        db.collection("historial_signos").add(nuevoRegistro)
    }
}
