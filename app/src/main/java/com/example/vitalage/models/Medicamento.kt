package com.example.vitalage.models

data class Medicamento(
    val id: String = "",
    val pacienteId: String = "",
    val nombre: String = "",
    val lote: String = "",
    val invima: String = "",
    val cantidad: Int = 0,
    val fechaVencimiento: String = "",
    val fechaInicio: String = "",
    val fechaFinalizacion: String = "",
    val observaciones: String = "",
    val auxiliar: String = ""
)
