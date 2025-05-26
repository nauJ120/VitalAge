package com.example.vitalage


import android.view.View
import android.widget.TextView
import com.example.vitalage.adapters.MedicalControlAdapter
import com.example.vitalage.model.MedicalControl
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.junit.Test

@RunWith(MockitoJUnitRunner::class)
class MedicalControlAdapterTest {

    @Test
    fun onBindViewHolder_shouldBindDataCorrectly() {
        val nameTv = mock<TextView>()
        val lotTv = mock<TextView>()
        val invimaTv = mock<TextView>()
        val quantityTv = mock<TextView>()
        val expirationTv = mock<TextView>()
        val startTv = mock<TextView>()
        val endTv = mock<TextView>()
        val obsTv = mock<TextView>()
        val nurseTv = mock<TextView>()

        val view = mock<View> {
            on { findViewById<TextView>(R.id.tvMedicationName) } doReturn nameTv
            on { findViewById<TextView>(R.id.tvMedicationLot) } doReturn lotTv
            on { findViewById<TextView>(R.id.tvMedicationInvima) } doReturn invimaTv
            on { findViewById<TextView>(R.id.tvMedicationQuantity) } doReturn quantityTv
            on { findViewById<TextView>(R.id.tvMedicationExpirationDate) } doReturn expirationTv
            on { findViewById<TextView>(R.id.tvMedicationStartDate) } doReturn startTv
            on { findViewById<TextView>(R.id.tvMedicationEndDate) } doReturn endTv
            on { findViewById<TextView>(R.id.tvMedicationObservations) } doReturn obsTv
            on { findViewById<TextView>(R.id.tvMedicationNurse) } doReturn nurseTv
        }

        val holder = MedicalControlAdapter.MedicalControlViewHolder(view)

        val control = MedicalControl(
            name = "Ibuprofeno",
            lot = "B2024",
            invima = "INV-8888",
            quantity = 20,
            expirationDate = "2025-05-01",
            startDate = "2024-05-01",
            endDate = "2024-12-01",
            observations = "Tomar después de comer",
            nurse = "Carlos Gómez",
            morningTime = "08:00",
            afternoonTime = "14:00",
            nightTime = "20:00",
            dosis = 2
        )

        val adapter = MedicalControlAdapter(listOf(control))

        adapter.onBindViewHolder(holder, 0)

        verify(nameTv).text = "Ibuprofeno"
        verify(lotTv).text = "B2024"
        verify(invimaTv).text = "INV-8888"
        verify(quantityTv).text = "20"
        verify(expirationTv).text = "2025-05-01"
        verify(startTv).text = "2024-05-01"
        verify(endTv).text = "2024-12-01"
        verify(obsTv).text = "Tomar después de comer"
        verify(nurseTv).text = "Carlos Gómez"
    }
}