package com.example.vitalage

import com.example.vitalage.model.DoseHistory
import com.example.vitalage.adapters.DoseHistoryAdapter
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class DoseHistoryAdapterTest {

    @Test
    fun testGetItemCount_returnsCorrectSize() {
        val mockData = listOf(
            createMockDose("Paracetamol"),
            createMockDose("Ibuprofeno")
        )
        val adapter = DoseHistoryAdapter(mockData)
        assertEquals(2, adapter.itemCount)
    }





    @Test
    fun testAdapter_withEmptyList_noCrash() {
        val adapter = DoseHistoryAdapter(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    private fun createMockDose(name: String): DoseHistory {
        return DoseHistory(
            medicamento = name,
            cantidad = 1,
            dosis = 250,
            observaciones = "Ninguna",
            usuario = "Enfermero A",
            fecha_hora = Timestamp(Date())
        )
    }
}