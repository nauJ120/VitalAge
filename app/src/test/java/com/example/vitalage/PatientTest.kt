package com.example.vitalage

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.vitalage.adapters.Patient
import com.example.vitalage.adapters.PatientAdapter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor

@RunWith(MockitoJUnitRunner::class)
class PatientAdapterTest {

    @Test
    fun `getItemCount returns correct list size`() {
        val patients = listOf(
            Patient("Juan Pérez", "123", "Masculino", 30),
            Patient("Ana López", "456", "Femenino", 25)
        )
        val adapter = PatientAdapter(patients) {}
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `bind sets patient data correctly`() {
        val nameText = mock(TextView::class.java)
        val detailsText = mock(TextView::class.java)
        val icon = mock(ImageView::class.java)
        val view = mock(View::class.java)

        // Simula los findViewById que se hacen en el ViewHolder
        `when`(view.findViewById<TextView>(R.id.tv_patient_name)).thenReturn(nameText)
        `when`(view.findViewById<TextView>(R.id.tv_patient_details)).thenReturn(detailsText)
        `when`(view.findViewById<ImageView>(R.id.iv_patient_icon)).thenReturn(icon)

        val patient = Patient("Carlos Ruiz", "789", "Masculino", 40)
        val adapter = PatientAdapter(listOf(patient)) {}
        val holder = adapter.PatientViewHolder(view)

        holder.bind(patient)

        verify(nameText).text = "Carlos Ruiz"
        verify(detailsText).text = "ID: 789 | Sexo: Masculino | Edad: 40"
        verify(icon).setImageResource(R.drawable.ic_user)
    }

    @Test
    fun `bind sets click listener correctly`() {
        val nameText = mock(TextView::class.java)
        val detailsText = mock(TextView::class.java)
        val icon = mock(ImageView::class.java)
        val view = mock(View::class.java)

        // Simula findViewById
        `when`(view.findViewById<TextView>(R.id.tv_patient_name)).thenReturn(nameText)
        `when`(view.findViewById<TextView>(R.id.tv_patient_details)).thenReturn(detailsText)
        `when`(view.findViewById<ImageView>(R.id.iv_patient_icon)).thenReturn(icon)

        val patient = Patient("Luisa Márquez", "321", "Femenino", 28)

        var clickedPatient: Patient? = null
        val adapter = PatientAdapter(listOf(patient)) { clickedPatient = it }
        val holder = adapter.PatientViewHolder(view)

        // Captura manualmente el click listener y lo ejecuta
        val captor = argumentCaptor<View.OnClickListener>()
        doNothing().`when`(view).setOnClickListener(captor.capture())

        holder.bind(patient)
        captor.firstValue.onClick(view)  // Esto simula el clic real

        assertEquals(patient, clickedPatient)
    }
}