package com.example.vitalage

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.assertEquals

@RunWith(MockitoJUnitRunner::class)
class NotesAdapterTest {

    @Test
    fun pruebadetamano() {
        val notes = listOf("Nota 1", "Nota 2", "Nota 3")
        val adapter = NotesAdapter(notes)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun pruebadevista() {
        val noteTextView = mock(TextView::class.java)
        val view = mock(View::class.java)

        `when`(view.findViewById<TextView>(R.id.tv_note)).thenReturn(noteTextView)

        val adapter = NotesAdapter(listOf("Hola Mundo"))
        val holder = adapter.NotesViewHolder(view)

        // Ejecutar bind
        holder.bind("Hola Mundo")

        // Verificar asignación de texto
        verify(noteTextView).text = "Hola Mundo"
    }
}