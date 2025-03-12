package com.example.vitalage.model

import com.google.firebase.Timestamp

data class DoseHistory(
    val medicamento: String = "",
    val cantidad: Int = 0,
    val dosis: Int = 0,
    val fecha_hora: Timestamp? = null,
    val usuario: String = "",
    val observaciones: String = ""
)
