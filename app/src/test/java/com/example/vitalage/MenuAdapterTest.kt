package com.example.vitalage

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.assertEquals

@RunWith(MockitoJUnitRunner::class)
class MenuAdapterTest {

    @Test
    fun pruebadetamano() {
        val options = listOf(
            MenuOption("Option1", 1),
            MenuOption("Option2", 2)
        )
        val adapter = MenuAdapter(options) { }
        assertEquals(options.size, adapter.itemCount)
    }

    @Test
    fun pruebadevista() {
        // Mock views
        val title = mock(TextView::class.java)
        val icon = mock(ImageView::class.java)
        val view = mock(View::class.java)
        `when`(view.findViewById<TextView>(R.id.tv_option_title)).thenReturn(title)
        `when`(view.findViewById<ImageView>(R.id.iv_option_icon)).thenReturn(icon)

        // Crear ViewHolder con la vista mockeada
        val adapter = MenuAdapter(emptyList()) { }
        val holder = adapter.MenuViewHolder(view)

        // Crear opción de menú de prueba
        val option = MenuOption("Test Option", 123)

        // Llamar a bind
        holder.bind(option)

        // Verificar que se asignaron los valores
        verify(title).text = "Test Option"
        verify(icon).setImageResource(123)
        verify(view).setOnClickListener(any())
    }
}