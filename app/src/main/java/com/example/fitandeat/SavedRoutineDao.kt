package com.example.fitandeat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedRoutineDao {

    @Insert
    suspend fun insert(savedRoutine: SavedRoutine)

    @Query("SELECT * FROM saved_routines WHERE email = :email")
    suspend fun getRoutinesByUser(email: String): List<SavedRoutine>

    @Query("DELETE FROM saved_routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: Int)
}
