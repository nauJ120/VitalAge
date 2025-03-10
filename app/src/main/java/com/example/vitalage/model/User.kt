package com.example.vitalage.model

class User(
    var tipodocumento: String = "",
    var identificacion: String = "",
    var correo: String = "",
    var contrasenia: String = "",
    var rol : String = "",
    var nombre_usuario : String = "",
    var listaPacientes: MutableMap<String, Paciente> = mutableMapOf()
)