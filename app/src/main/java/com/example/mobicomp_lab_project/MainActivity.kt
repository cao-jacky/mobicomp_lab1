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

    lateinit var geofencingClient: GeofencingClient

    private var geofenceList: ArrayList<Geofence> = ArrayList<Geofence>()

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val timeClass = TimeActivity()
        val mapClass = MapActivity()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getAppDataBase(context = this)
        ReminderDao = db?.reminderDao()

        geofencingClient = LocationServices.getGeofencingClient(this)

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

//                Log.d("debugging", "placing item with message "+rIItem)

                var alarmItemIntent = Intent(this, AlarmReceiver::class.java)
                alarmItemIntent.putExtra("Reminder", rIItem)

                pendingIntent = PendingIntent.getBroadcast(this, i, alarmItemIntent, 0)
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val cal = Calendar.getInstance()
                val rITime = reminderItem.time
                cal.timeInMillis = rITime!!

                val rILoc: String = reminderItem.location.toString()

                if (rITime == 0.toLong()) {
                    // If a location-based reminder, set a geofence
//                    Log.d("debugging", rILoc)
                    geofenceList.add(Geofence.Builder()
                        .setRequestId(i.toString())
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                            rILoc.substringAfter("(").substringBefore(',').toDouble(),
                            rILoc.substringAfter(",").substringBefore(')').toDouble(),
                            10F
                        )

                        .setExpirationDuration(10000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build())

                    geofencingClient?.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
                        addOnSuccessListener {
                            // Geofences added
                            // ...
                        }
                        addOnFailureListener {
                            // Failed to add geofences
                            // ...
                        }
                    }

                } else {
                    // If a time-based reminder, set an alarm
                    val currTime = System.currentTimeMillis()
                    if (rITime.toInt() > currTime.toInt()) {
                        alarmManager.setExact(AlarmManager.RTC, cal.timeInMillis, pendingIntent)
                    }
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

