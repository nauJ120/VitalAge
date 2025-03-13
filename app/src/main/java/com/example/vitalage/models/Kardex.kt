package com.example.vitalage.models

data class Kardex(
    val id: String = "",
    val pacienteId: String = "",
    val diagnostico: String = "",
    val alergias: String = "",
    val notasMedicas: String = "",
    val fechaActualizacion: String = ""
)