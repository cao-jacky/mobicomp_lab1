package com.example.mobicomp_lab1

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
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import android.media.RingtoneManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.room.*
import java.util.*
import com.example.mobicomp_lab1.dao.ReminderDao
import com.example.mobicomp_lab1.dataBase.AppDatabase
import com.example.mobicomp_lab1.entity.Reminder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

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

            // Creating the pending intent to send to the BroadcastReceiver
//            alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            val intent = Intent(this, AlarmReceiver::class.java)
//            pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//            // Setting the specific time for the alarm manager to trigger the intent, in this example, the alarm is set to go off at 23:30, update the time according to your need
//            val calendar = Calendar.getInstance()
//            calendar.timeInMillis = System.currentTimeMillis()
//            calendar.set(Calendar.HOUR_OF_DAY, 20)
//            calendar.set(Calendar.MINUTE, 52)
//
//            alarmManager.set(
//                AlarmManager.RTC,
//                calendar.timeInMillis,
//                pendingIntent
//            )

//            for (i in 0 until queryDatabase?.size!!) {
//                val reminderItem = queryDatabase?.get(i)
//                val rIUID = reminderItem.uid.toString()
//                val rITime = reminderItem.time.toString()
//                val rILoc = reminderItem.location.toString()
//                val rIMess = reminderItem.message.toString()
//
//                val rIArray = arrayOf(rIUID, rITime, rILoc, rIMess)
//
//                listItems[i] = rIUID?.toInt()
//            }

            for (i in 0 until queryDatabase?.size!!) {
                val reminderItem = queryDatabase?.get(i)
                val rIItem = reminderItem.message

                var alarmItemIntent = Intent(this, AlarmReceiver::class.java)
                alarmItemIntent.putExtra("Reminder", rIItem)

                pendingIntent = PendingIntent.getBroadcast(this, i, alarmItemIntent, 0)
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val cal = Calendar.getInstance()
                val rITime = reminderItem.time
                cal.timeInMillis = rITime!!

                val currTime = System.currentTimeMillis()
                if (rITime.toInt() > currTime.toInt()) {
                    alarmManager.setExact(AlarmManager.RTC, cal.timeInMillis, pendingIntent)
                }

            }

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        val timeButton = findViewById<Button>(R.id.time_button)
//        timeButton?.setOnClickListener()
//        {
//            val sysCurrTime = timeClass.pullCurrentTime()
//            Toast.makeText(this@MainActivity,
//                sysCurrTime, Toast.LENGTH_LONG).show() }
//
//        val locButton = findViewById<Button>(R.id.location_button)
//        locButton?.setOnClickListener()
//        {
//            getLastLocation()
//        }
//
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

//    @SuppressLint("MissingPermission")
//    fun getLastLocation() {
//        if (checkPermissions()) {
//            if (isLocationEnabled()) {
//
//                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
//                    var location: Location? = task.result
//                    if (location == null) {
//                        requestNewLocationData()
//                    } else {
//                        findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
//                        findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)
//            }
//        } else {
//            requestPermissions()
//        }
//    }

//    @SuppressLint("MissingPermission")
//    private fun requestNewLocationData() {
//        var mLocationRequest = LocationRequest()
//        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        mLocationRequest.interval = 0
//        mLocationRequest.fastestInterval = 0
//        mLocationRequest.numUpdates = 1
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        mFusedLocationClient!!.requestLocationUpdates(
//            mLocationRequest, mLocationCallback,
//            Looper.myLooper()
//        )
//    }
//
//    private val mLocationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            var mLastLocation: Location = locationResult.lastLocation
//            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
//            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
//        }
//    }

//    private fun isLocationEnabled(): Boolean {
//        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//            LocationManager.NETWORK_PROVIDER
//        )
//    }
//
//    private fun checkPermissions(): Boolean {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//        }
//        return false
//    }
//
//    private fun requestPermissions() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
//            PERMISSION_ID
//        )
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (requestCode == PERMISSION_ID) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                getLastLocation()
//            }
//        }
//    }
}

