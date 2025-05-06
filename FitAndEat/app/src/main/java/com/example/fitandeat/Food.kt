package com.example.fitandeat

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "foods",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["email"],
        childColumns = ["email"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Food(
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val email: String, // ← Este será el identificador del usuario dueño de esta comida
    val nombre: String,
    val calorias: Int,
    val proteinas: Int,
    val carbohidratos: Int,
    val grasas: Int
)

