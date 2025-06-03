package com.example.fitandeat.user.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val nombre: String,
    val edad: Int,
    val peso: Float,
    val estatura: Float,
    val sexo: String,
    val objetivo: String
)