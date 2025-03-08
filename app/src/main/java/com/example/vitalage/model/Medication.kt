package com.example.vitalage.model

data class Medication(
    var id: String = "",  // Para almacenar el ID de Firestore
    var nombre: String = "",
    var cantidad: Int = 0,  // 🔥 Aquí aseguramos que sea 'cantidad' como en Firestore
    var dosis: Int = 0,
    var enfermero: String = "",
    var fecha_fin: String = "",
    var fecha_inicio: String = "",
    var fecha_vencimiento: String = "",
    var hora_mañana: String = "",
    var hora_noche: String = "",
    var hora_tarde: String = "",
    var invima: String = "",
    var lote: String = "",
    var observaciones: String = ""
)
