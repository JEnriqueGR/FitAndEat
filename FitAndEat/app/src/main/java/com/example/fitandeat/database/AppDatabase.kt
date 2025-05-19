package com.example.fitandeat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitandeat.exercise.dao.SavedRoutineDao
import com.example.fitandeat.exercise.dao.SavedTrainDao
import com.example.fitandeat.food.dao.FoodDao
import com.example.fitandeat.exercise.model.SesionTrain
import com.example.fitandeat.exercise.dao.SesionTrainDao
import com.example.fitandeat.exercise.model.SavedRoutine
import com.example.fitandeat.exercise.model.SavedTrain
import com.example.fitandeat.food.model.Food
import com.example.fitandeat.user.model.User
import com.example.fitandeat.user.dao.UserDao

@Database(entities = [User::class, SesionTrain::class, Food::class, SavedTrain::class, SavedRoutine::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionTrainDao(): SesionTrainDao
    abstract fun foodDao(): FoodDao
    abstract fun savedTrainDao(): SavedTrainDao
    abstract fun savedRoutineDao(): SavedRoutineDao


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