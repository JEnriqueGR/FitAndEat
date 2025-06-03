package com.example.fitandeat.food.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitandeat.food.model.Food

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarComida(food: Food)

    @Query("SELECT * FROM foods WHERE email = :correo")
    suspend fun obtenerComidasPorUsuario(correo: String): List<Food>

    @Delete
    suspend fun eliminarComida(food: Food)
}