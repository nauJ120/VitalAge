package com.example.vitalage

import org.junit.Assert.assertEquals
import org.junit.Test



class DatePickerFragmentTest {

    @Test
    fun `formatea correctamente la fecha`() {
        val resultado = formatDate(2, 0, 2023)
        assertEquals("02/01/2023", resultado)
    }





    fun formatDate(day: Int, month: Int, year: Int): String {
        return "${day.toString().padStart(2, '0')}/" +
                "${(month + 1).toString().padStart(2, '0')}/$year"
    }
}