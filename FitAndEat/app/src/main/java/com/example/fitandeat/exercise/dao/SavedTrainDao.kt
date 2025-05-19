package com.example.fitandeat.exercise.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitandeat.exercise.model.SavedTrain

@Dao
interface SavedTrainDao {

    @Insert
    suspend fun insert(savedTrain: SavedTrain)

    @Query("SELECT * FROM saved_trains WHERE email = :email")
    suspend fun getTrainsByUser(email: String): List<SavedTrain>

    @Query("DELETE FROM saved_trains WHERE id = :trainId")
    suspend fun deleteTrainById(trainId: Int)

}
