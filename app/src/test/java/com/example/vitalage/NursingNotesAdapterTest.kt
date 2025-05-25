package com.example.vitalage

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.vitalage.adapters.NursingNotesAdapter
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.assertEquals

@RunWith(MockitoJUnitRunner::class)
class NursingNotesAdapterTest {

    @Test
    fun probartamanos() {
        val notes = listOf(
            mapOf("titulo" to "Nota 1"),
            mapOf("titulo" to "Nota 2")
        )
        val adapter = NursingNotesAdapter(notes, "1234") {}
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun probarformatocorrecto() {
        val noteText = mock(TextView::class.java)
        val btnDelete = mock(ImageView::class.java)
        val view = mock(View::class.java)

        `when`(view.findViewById<TextView>(R.id.tv_note)).thenReturn(noteText)
        `when`(view.findViewById<ImageView>(R.id.btn_delete)).thenReturn(btnDelete)

        val note = mapOf(
            "titulo" to "Control de presión",
            "fecha" to "2024-05-14",
            "enfermera" to "Laura M.",
            "descripcion" to "Presión normal"
        )

        val adapter = NursingNotesAdapter(listOf(note), "1234") {}
        val holder = adapter.NursingNoteViewHolder(view)

        holder.bind(note)

        verify(noteText).text = "📌 Control de presión\n2024-05-14 - Laura M.: Presión normal"
    }
}