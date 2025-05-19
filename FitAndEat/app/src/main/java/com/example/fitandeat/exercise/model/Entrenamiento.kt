package com.example.fitandeat.exercise.model

data class Serie(
    val numero: Int,
    val nombreEjercicio: String,
    val kg: Float,
    val repeticiones: Int,
    val completada: Boolean
)

data class Entrenamiento(
    val nombreEntrenamiento: String,
    val series: MutableList<Serie> = mutableListOf()
)
