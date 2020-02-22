package com.example.mobicomp_lab_project

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.google.android.gms.location.*
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import java.util.*
import com.example.mobicomp_lab_project.dao.ReminderDao
import com.example.mobicomp_lab_project.dataBase.AppDatabase
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var remindersListView: ListView

    private var db: AppDatabase? = null
    private var ReminderDao: ReminderDao? = null

    private val REQUEST_CODE = 100
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        val timeClass = TimeActivity()
        val mapClass = MapActivity()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getAppDataBase(context = this)
        ReminderDao = db?.reminderDao()

        Observable.fromCallable {

            val aTR: View = findViewById(R.id.addTimeReminder)
            aTR.setOnClickListener { view ->
                val new_reminder_intent = Intent(this, TimeActivity::class.java)
                startActivity(new_reminder_intent)

            }

            remindersListView = findViewById<ListView>(R.id.remindersListView)
            var queryDatabase = db?.reminderDao()?.getAll()
            val listItems = arrayOf(queryDatabase?.size)
            val adapter = listAdapter(this, ArrayList(queryDatabase!!))
            remindersListView.adapter = adapter

            for (i in 0 until queryDatabase?.size!!) {
                val reminderItem = queryDatabase?.get(i)
                val rIItem = reminderItem.message

                Log.d("debugging", "placing item with message "+rIItem)

                var alarmItemIntent = Intent(this, AlarmReceiver::class.java)
                alarmItemIntent.putExtra("Reminder", rIItem)

                pendingIntent = PendingIntent.getBroadcast(this, i, alarmItemIntent, 0)
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val cal = Calendar.getInstance()
                val rITime = reminderItem.time
                cal.timeInMillis = rITime!!

                val currTime = System.currentTimeMillis()
                Log.d("debugging", rITime.toInt().toString()+" "+currTime.toInt().toString())
                if (rITime.toInt() > currTime.toInt()) {
                    Log.d("debugging", "setting alarm")
                    alarmManager.setExact(AlarmManager.RTC, cal.timeInMillis, pendingIntent)
                }

            }

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

//        val timeButton = findViewById<Button>(R.id.time_button)
//        timeButton?.setOnClickListener()
//        {
//            val sysCurrTime = timeClass.pullCurrentTime()
//            Toast.makeText(this@MainActivity,
//                sysCurrTime, Toast.LENGTH_LONG).show() }

        val locButton = findViewById<Button>(R.id.addLocationReminder)
        locButton?.setOnClickListener()
        {
            setContentView(R.layout.activity_map)

            var mapsIntent = Intent(this,MapActivity::class.java)
            startActivity(mapsIntent)
        }

//        val playMediaButton = findViewById<Button>(R.id.sound_button)
//        playMediaButton?.setOnClickListener()
//        {
//            try {
//                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//                val r = RingtoneManager.getRingtone(applicationContext, notification)
//                r.play()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//        }

    }

}

