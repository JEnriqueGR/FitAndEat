package com.example.fitandeat

data class Rutina(
    val nombre: String,
    val ejercicios: List<EjercicioRutina>
)

data class EjercicioRutina(
    val nombreEjercicio: String,
    val series: List<SerieRutina>
)

data class SerieRutina(
    val numero: Int,
    val nombreEjercicio: String,
    val kg: Float,
    val repeticiones: Int,
    val completada: Boolean
)

