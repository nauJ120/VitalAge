package com.example.vitalage.model

data class MedicalControl(
    val name: String,
    val lot: String,
    val invima: String,
    val quantity: Int,
    val expirationDate: String,
    val startDate: String,
    val endDate: String,
    val observations: String,
    val nurse: String,
    val morningTime: String,
    val afternoonTime: String,
    val nightTime: String,
    val dosis: Int
)
