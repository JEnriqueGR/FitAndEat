package com.example.fitandeat

import androidx.room.*

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarComida(food: Food)

    @Query("SELECT * FROM foods WHERE email = :correo")
    suspend fun obtenerComidasPorUsuario(correo: String): List<Food>

    @Delete
    suspend fun eliminarComida(food: Food)
}
