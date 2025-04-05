package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.EmailDao
import com.example.myapplication.data.dao.PersonalDataDao
import com.example.myapplication.data.entity.EmailEntity
import com.example.myapplication.data.entity.PersonalDataEntity

@Database(entities = [EmailEntity::class, PersonalDataEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao
    abstract fun personalDataDao(): PersonalDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "briefy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}