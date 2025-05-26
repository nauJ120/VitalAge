package com.example.vitalage

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.vitalage.adapters.MedicationAdapter
import com.example.vitalage.model.Medication
import org.junit.Test
import org.junit.runner.RunWith

import org.mockito.kotlin.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MedicationAdapterTest {

    @Test
    fun `onBindViewHolder should bind data correctly and set click listener`() {
        // Mock views
        val nameTv = mock<TextView>()
        val quantityTv = mock<TextView>()
        val btnAdminister = mock<Button>()

        val view = mock<View> {
            on { findViewById<TextView>(R.id.tvMedicationName) } doReturn nameTv
            on { findViewById<TextView>(R.id.tvMedicationQuantity) } doReturn quantityTv
            on { findViewById<Button>(R.id.btnAdminister) } doReturn btnAdminister
        }

        // Crear ViewHolder con el view mockeado
        val holder = MedicationAdapter.MedicationViewHolder(view)

        // Crear medicamento de prueba
        val medication = Medication(nombre = "Paracetamol", cantidad = 10)

        // Crear mock para callback
        val callback: (Medication) -> Unit = mock()

        // Crear Adapter con una lista con un solo medicamento
        val adapter = MedicationAdapter(mutableListOf(medication), callback)

        // Ejecutar método a testear
        adapter.onBindViewHolder(holder, 0)

        // Verificar que los textos se asignaron correctamente
        verify(nameTv).text = "Paracetamol"
        verify(quantityTv).text = "10"

        // Capturar el listener del botón y probar que llame al callback con el medicamento correcto
        val captor = argumentCaptor<View.OnClickListener>()
        verify(btnAdminister).setOnClickListener(captor.capture())

        // Simular click
        captor.firstValue.onClick(null)

        // Verificar que callback se haya llamado con el medicamento correcto
        verify(callback).invoke(medication)
    }

    @Test
    fun `updateData should update list and notify changes`() {
        val initialList = mutableListOf(
            Medication(nombre = "Med1", cantidad = 1),
            Medication(nombre = "Med2", cantidad = 2)
        )
        val callback: (Medication) -> Unit = mock()

        val adapter = spy(MedicationAdapter(initialList, callback))

        // Anular notifyDataSetChanged para que no lance excepción
        doNothing().`when`(adapter).notifyDataSetChanged()

        val newList = listOf(
            Medication(nombre = "Med3", cantidad = 3)
        )

        adapter.updateData(newList)

        // Verificar que la lista interna fue actualizada
        assert(adapter.itemCount == 1)
        assert(adapter.getMedications()[0].nombre == "Med3")

        // Verificar que notifyDataSetChanged fue llamado
        verify(adapter).notifyDataSetChanged()
    }
}