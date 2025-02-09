package com.example.vitalage.models

data class SignoVital(
    val id: String = "",
    val pacienteId: String = "",
    val fecha: String = "",
    val presionArterial: String = "",
    val frecuenciaCardiaca: Int = 0,
    val temperatura: Float = 0.0f,
    val oxigenacion: Int = 0
)
