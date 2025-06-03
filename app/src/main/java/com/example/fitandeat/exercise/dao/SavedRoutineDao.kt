package com.example.fitandeat.exercise.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitandeat.exercise.model.SavedRoutine

@Dao
interface SavedRoutineDao {

    @Insert
    suspend fun insert(savedRoutine: SavedRoutine)

    @Query("SELECT * FROM saved_routines WHERE email = :email")
    suspend fun getRoutinesByUser(email: String): List<SavedRoutine>

    @Query("DELETE FROM saved_routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: Int)
}
