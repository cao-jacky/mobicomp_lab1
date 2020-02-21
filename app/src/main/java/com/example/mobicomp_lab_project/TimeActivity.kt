package com.example.mobicomp_lab_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.mobicomp_lab_project.dao.ReminderDao
import com.example.mobicomp_lab_project.dataBase.AppDatabase
import com.example.mobicomp_lab_project.entity.Reminder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


class TimeActivity : AppCompatActivity() {

    private var db: AppDatabase? = null
    private var ReminderDao: ReminderDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_time_reminder_view)

        val aTR: View = findViewById(R.id.submitTimeReminder)
        aTR.setOnClickListener { view ->
            val timeReminderText: TextView = findViewById(R.id.timeReminderText)
            val tRTString: String = timeReminderText.text.toString()

            val timeReminderDate: CalendarView = findViewById(R.id.dateView)
            val tRDString: Long = timeReminderDate.date

            val timeReminderTime: TimePicker = findViewById(R.id.timePicker)
            val hour: Int = timeReminderTime.hour
            val min: Int = timeReminderTime.minute

            val cal = Calendar.getInstance()
            cal.timeInMillis = tRDString
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)

            val setDateTime: Long = cal.timeInMillis

            db = AppDatabase.getAppDataBase(context = this)
            ReminderDao = db?.reminderDao()

            Observable.fromCallable {
                var newTimeReminder = Reminder(null, location = "0", time = setDateTime, message = tRTString)

                with(ReminderDao) {
                    this?.insertAll(newTimeReminder)
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

            val newMainActivity = Intent(this, MainActivity::class.java)
            finish()
            startActivity(newMainActivity)

//            var queryDatabase = db?.reminderDao()?.getAll()
//            val adapter = listAdapter(this, ArrayList(queryDatabase!!))
//            adapter.notifyDataSetChanged()

        }
    }
}