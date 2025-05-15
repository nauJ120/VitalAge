package com.example.vitalage

import android.widget.TextView
import com.example.vitalage.databinding.ItemDoseHistoryBinding
import com.example.vitalage.model.DoseHistory
import com.example.vitalage.DoseHistoryAdapter
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.text.SimpleDateFormat
import java.util.*
import androidx.test.core.app.ApplicationProvider
import org.mockito.Mockito.`when`
import org.mockito.ArgumentCaptor
import org.mockito.Answers
import org.mockito.Mock
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout








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