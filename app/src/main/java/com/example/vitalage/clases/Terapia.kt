package com.example.vitalage.clases

data class Terapia(
    val id: String = "",               // ID de la terapia en Firestore
    val tipo: String = "",             // Tipo de terapia (Física, Ocupacional, Respiratoria, etc.)
    val descripcion: String = "",      // Descripción de la terapia
    val doctor: String = "",           // Nombre del doctor que realizó la terapia
    val fecha: String = "",            // Fecha de la terapia
    val encargado: String = "",        // Nombre del encargado que realizó la terapia
    val realizada: Boolean = false     // Indica si la terapia se realizó o no
)
