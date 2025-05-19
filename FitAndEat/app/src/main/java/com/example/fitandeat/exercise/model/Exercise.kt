package com.example.fitandeat.exercise.model

import java.io.Serializable

data class Exercise(
    val nombre: String,
    val descripcion: String,
    val imagenResId: Int,
    val alternativas: List<AlternativeExercise>,
    val musculo: String
) : Serializable

data class AlternativeExercise(
    val nombre: String,
    val imagenResId: Int
) : Serializable
