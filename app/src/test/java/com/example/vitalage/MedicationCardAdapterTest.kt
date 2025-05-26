package com.example.vitalage

import android.view.View
import android.widget.TextView
import com.example.vitalage.adapters.MedicationCardAdapter
import com.example.vitalage.adapters.MedicationCardAdapter.MedicationCardViewHolder
import com.example.vitalage.model.MedicationCard
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.assertEquals


@RunWith(MockitoJUnitRunner::class)
class MedicationCardAdapterTest {

    @Test
    fun pruebadetamano() {
        val meds = listOf(
            MedicationCard("Med1", "2 pills", "After meals", "08:00", "14:00", "20:00"),
            MedicationCard("Med2", "1 pill", "Before bed", "09:00", "15:00", "21:00")
        )
        val adapter = MedicationCardAdapter(meds)
        assertEquals(meds.size, adapter.itemCount)
    }

    @Test
    fun pruebadevista() {
        val tvName = mock(TextView::class.java)
        val tvDose = mock(TextView::class.java)
        val tvObservation = mock(TextView::class.java)
        val tvSchedule = mock(TextView::class.java)

        val view = mock(View::class.java)
        `when`(view.findViewById<TextView>(R.id.tvMedicationName)).thenReturn(tvName)
        `when`(view.findViewById<TextView>(R.id.tvMedicationDose)).thenReturn(tvDose)
        `when`(view.findViewById<TextView>(R.id.tvMedicationObservation)).thenReturn(tvObservation)
        `when`(view.findViewById<TextView>(R.id.tvMedicationSchedule)).thenReturn(tvSchedule)

        val holder = MedicationCardViewHolder(view)

        val med = MedicationCard(
            name = "Ibuprofeno",
            dose = "2 pills",
            observation = "Tomar con agua",
            morning = "08:00",
            afternoon = "14:00",
            night = "20:00"
        )

        val adapter = MedicationCardAdapter(listOf(med))

        adapter.onBindViewHolder(holder, 0)

        verify(tvName).text = "Ibuprofeno" // Esto puede fallar, mejor usa setText
        verify(tvName).setText("Ibuprofeno")
        verify(tvDose).setText("2 pills")
        verify(tvObservation).setText("Tomar con agua")
        verify(tvSchedule).setText("08:00 • 14:00 • 20:00")
    }
}