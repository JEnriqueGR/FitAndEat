package com.example.fitandeat.food.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fitandeat.user.model.User

@Entity(
    tableName = "foods",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["email"],
        childColumns = ["email"],
        onDelete = ForeignKey.Companion.CASCADE
    )]
)
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String, // ← Este será el identificador del usuario dueño de esta comida
    val nombre: String,
    val calorias: Int,
    val proteinas: Int,
    val carbohidratos: Int,
    val grasas: Int
)