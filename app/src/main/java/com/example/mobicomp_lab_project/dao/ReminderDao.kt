package com.example.mobicomp_lab_project.dao

import androidx.room.*
import com.example.mobicomp_lab_project.entity.Reminder

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAll(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE message LIKE :message")
    fun findByMessage(message: String): Reminder

    @Query("SELECT * FROM reminders WHERE uid LIKE :uid")
    fun findByUID(uid: Int?): Reminder

    @Insert
    fun insertAll(vararg reminder: Reminder)

    @Delete
    fun delete(reminder: Reminder)

    @Update
    fun updateReminder(vararg reminders: Reminder)
}
