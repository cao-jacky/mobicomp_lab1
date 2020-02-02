package com.example.mobicomp_lab1

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.*
import androidx.room.*
import java.util.*
import com.example.mobicomp_lab1.dao.ReminderDao
import com.example.mobicomp_lab1.dataBase.AppDatabase
import com.example.mobicomp_lab1.entity.Reminder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var listView: ListView

    private var db: AppDatabase? = null
    private var ReminderDao: ReminderDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val timeClass = TimeActivity()
        val mapClass = MapActivity()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getAppDataBase(context = this)
        ReminderDao = db?.reminderDao()

        Observable.fromCallable {
//            db = AppDatabase.getAppDataBase(context = this)
//            ReminderDao = db?.reminderDao()

//            var item1 = Reminder(location = "here", time = System.currentTimeMillis(), message = "test")
//
//            with(ReminderDao){
//                this?.insertAll(item1)
//            }
            var queryDatabase = db?.reminderDao()?.getAll()
            Log.d("debugging", queryDatabase.toString())
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

//        listView = findViewById<ListView>(R.id.listView)
//        val recipeList = Recipe.getRecipesFromFile("recipes.json", this)
//
//        val listItems = arrayOfNulls<String>(recipeList.size)
//
//        for (i in 0 until recipeList.size) {
//            val recipe = recipeList[i]
//            listItems[i] = recipe.title
//        }
//
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
//        listView.adapter = adapter

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val timeButton = findViewById<Button>(R.id.time_button)
        timeButton?.setOnClickListener()
        {
            val sysCurrTime = timeClass.pullCurrentTime()
            Toast.makeText(this@MainActivity,
                sysCurrTime, Toast.LENGTH_LONG).show() }

        val locButton = findViewById<Button>(R.id.location_button)
        locButton?.setOnClickListener()
        {
            getLastLocation()
        }

        val playMediaButton = findViewById<Button>(R.id.sound_button)
        playMediaButton?.setOnClickListener()
        {
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                        findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
}

