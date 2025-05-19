package com.example.fitandeat.exercise.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitandeat.exercise.model.SesionTrain

@Dao
interface SesionTrainDao {

    @Insert
    suspend fun insertarSesion(sesion: SesionTrain)

    @Query("SELECT * FROM sesiones_entrenamiento WHERE email = :correo ORDER BY fecha ASC")
    suspend fun obtenerSesionesPorUsuario(correo: String): List<SesionTrain>
}