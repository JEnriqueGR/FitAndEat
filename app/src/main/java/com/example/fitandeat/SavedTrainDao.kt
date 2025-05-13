package com.example.fitandeat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedTrainDao {

    @Insert
    suspend fun insert(savedTrain: SavedTrain)

    @Query("SELECT * FROM saved_trains WHERE email = :email")
    suspend fun getTrainsByUser(email: String): List<SavedTrain>

    @Query("DELETE FROM saved_trains WHERE id = :trainId")
    suspend fun deleteTrainById(trainId: Int)

}
