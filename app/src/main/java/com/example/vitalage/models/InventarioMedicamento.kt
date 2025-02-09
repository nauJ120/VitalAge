package com.example.vitalage.models

data class InventarioMedicamento(
    val id: String = "",
    val nombre: String = "",
    val cantidadDisponible: Int = 0,
    val proveedor: String = "",
    val fechaVencimiento: String = ""
)