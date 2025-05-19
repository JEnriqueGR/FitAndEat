package com.example.fitandeat.exercise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fitandeat.user.model.User

@Entity(
    tableName = "sesiones_entrenamiento",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["email"],
        childColumns = ["email"],
        onDelete = ForeignKey.Companion.CASCADE
    )]
)
data class SesionTrain(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    val ejercicio: String,
    val series: Int,
    val repeticiones: Int,
    val peso: Float,
    val fecha: Long = System.currentTimeMillis()
)