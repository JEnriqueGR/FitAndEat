package com.example.fitandeat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, SesionTrain::class, Food::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionTrainDao(): SesionTrainDao
    abstract fun foodDao(): FoodDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitandeatv1_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}