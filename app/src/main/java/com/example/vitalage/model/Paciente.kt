package com.example.vitalage.model

import java.io.Serializable

class Paciente(
    var nombreCompleto: String = "",
    var correo: String = "",
    var edad: Int = 0,
    var imagenUrl: String = "",
    var listaTomas: MutableMap<String, Toma> = mutableMapOf()

): Serializable