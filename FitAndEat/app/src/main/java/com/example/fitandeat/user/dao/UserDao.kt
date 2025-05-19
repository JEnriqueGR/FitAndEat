package com.example.fitandeat.user.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitandeat.user.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarUsuario(user: User)

    @Query("SELECT * FROM users WHERE email = :correo")
    suspend fun obtenerUsuarioPorCorreo(correo: String): User?

    @Query("UPDATE users SET objetivo = :objetivo WHERE email = :correo")
    suspend fun actualizarObjetivo(correo: String, objetivo: String)

}