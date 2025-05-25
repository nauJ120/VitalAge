package com.example.vitalage

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.vitalage.adapters.UserAdapter
import com.example.vitalage.model.User
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.argumentCaptor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class UserAdapterTest {

    @Test
    fun establacerloslistenerscorrectamente() {
        // Mocks de vistas
        val nameText: TextView = mock()
        val emailText: TextView = mock()
        val roleText: TextView = mock()
        val btnEdit: ImageButton = mock()
        val btnDelete: ImageButton = mock()
        val view: View = mock()

        // Simula findViewById
        whenever(view.findViewById<TextView>(R.id.tvUserName)).thenReturn(nameText)
        whenever(view.findViewById<TextView>(R.id.tvUserEmail)).thenReturn(emailText)
        whenever(view.findViewById<TextView>(R.id.tvUserRole)).thenReturn(roleText)
        whenever(view.findViewById<ImageButton>(R.id.btnEditUser)).thenReturn(btnEdit)
        whenever(view.findViewById<ImageButton>(R.id.btnDeleteUser)).thenReturn(btnDelete)

        // Usuario de prueba
        val user = User(nombre_usuario = "Laura Gómez", correo = "laura@example.com", rol = "Administrador")

        // Variables para capturar eventos
        var editedUser: User? = null
        var deletedUser: User? = null

        val adapter = UserAdapter(
            listOf(user),
            onEditClick = { editedUser = it },
            onDeleteClick = { deletedUser = it }
        )

        val holder = adapter.UserViewHolder(view)

        // Captura los click listeners
        val editCaptor = argumentCaptor<View.OnClickListener>()
        val deleteCaptor = argumentCaptor<View.OnClickListener>()

        doNothing().whenever(btnEdit).setOnClickListener(editCaptor.capture())
        doNothing().whenever(btnDelete).setOnClickListener(deleteCaptor.capture())

        // Vincula el usuario al ViewHolder
        holder.bind(user)

        // Simula clicks
        editCaptor.firstValue.onClick(btnEdit)
        deleteCaptor.firstValue.onClick(btnDelete)

        // Verifica
        assertEquals(user, editedUser)
        assertEquals(user, deletedUser)
    }
}