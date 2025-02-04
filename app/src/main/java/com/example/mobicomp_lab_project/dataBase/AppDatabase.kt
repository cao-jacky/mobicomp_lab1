package com.example.mobicomp_lab_project.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobicomp_lab_project.dao.ReminderDao
import com.example.mobicomp_lab_project.entity.Reminder

@Database(entities = arrayOf(Reminder::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null){
                synchronized(AppDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "remindersList").build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}