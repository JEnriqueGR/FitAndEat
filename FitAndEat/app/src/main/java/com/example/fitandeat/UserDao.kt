package com.example.fitandeat

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(user: User)

    @Query("SELECT * FROM users WHERE email = :correo")
    suspend fun obtenerUsuarioPorCorreo(correo: String): User?

    @Query("UPDATE users SET objetivo = :objetivo WHERE email = :correo")
    suspend fun actualizarObjetivo(correo: String, objetivo: String)

}