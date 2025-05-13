package com.example.fitandeat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_routines")
data class SavedRoutine(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val email: String,
        val rutinaJson: String
)
