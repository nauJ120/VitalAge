package com.example.vitalage

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.vitalage.adapters.Resident
import com.example.vitalage.adapters.ResidentAdapter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.mockito.Mockito.*


class ResidentAdapterTest {

    @Test
    fun establacerloslistenerscorrectamente() {
        // Mocks de vistas
        val nameText: TextView = mock()
        val detailsText: TextView = mock()
        val idText: TextView = mock()
        val icon: ImageView = mock()
        val btnEdit: ImageButton = mock()
        val btnDelete: ImageButton = mock()
        val genderText: TextView = mock()
        val view: View = mock()

        // Simula findViewById
        whenever(view.findViewById<TextView>(R.id.tvResidentName)).thenReturn(nameText)
        whenever(view.findViewById<TextView>(R.id.tvResidentInfo)).thenReturn(detailsText)
        whenever(view.findViewById<TextView>(R.id.tvResidentId)).thenReturn(idText)
        whenever(view.findViewById<ImageView>(R.id.imgResident)).thenReturn(icon)
        whenever(view.findViewById<ImageButton>(R.id.btnEditResident)).thenReturn(btnEdit)
        whenever(view.findViewById<ImageButton>(R.id.btnDeleteResident)).thenReturn(btnDelete)
        whenever(view.findViewById<TextView>(R.id.tvResidentGender)).thenReturn(genderText)

        val resident = Resident("Andrés Londoño", "123456", "Masculino", 40)

        var editedResident: Resident? = null
        var deletedResident: Resident? = null

        val adapter = ResidentAdapter(
            listOf(resident),
            onEditClick = { editedResident = it },
            onDeleteClick = { deletedResident = it }
        )

        val holder = adapter.ResidentViewHolder(view)

        // Captura los click listeners
        val editCaptor = argumentCaptor<View.OnClickListener>()
        val deleteCaptor = argumentCaptor<View.OnClickListener>()

        doNothing().whenever(btnEdit).setOnClickListener(editCaptor.capture())
        doNothing().whenever(btnDelete).setOnClickListener(deleteCaptor.capture())

        // Vincula el residente al ViewHolder
        holder.bind(resident)

        // Simula clicks
        editCaptor.firstValue.onClick(btnEdit)
        deleteCaptor.firstValue.onClick(btnDelete)

        // Verifica
        assertEquals(resident, editedResident)
        assertEquals(resident, deletedResident)
    }
}