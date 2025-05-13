package com.example.vitalage.model

data class SignosVitales(
    val fecha: String = "",
    val frecuenciaCardiaca: Int = 0,
    val frecuenciaRespiratoria: Int = 0,
    val saturacionOxigeno: Int = 0,
    val presionArterial: Int = 0,
    val temperatura: Double = 0.0,
    val peso: Double = 0.0,
    val imc: Double = 0.0,
    val encargado: String = "",
    val escalaDolor: Int = 0
)
