package com.example.mobicomp_lab_project

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.mobicomp_lab_project.dao.ReminderDao
import com.example.mobicomp_lab_project.dataBase.AppDatabase
import com.example.mobicomp_lab_project.entity.Reminder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.location.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class MapActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    val PERMISSION_ID = 42
    private lateinit var map: GoogleMap
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var geofencingClient: GeofencingClient

    private var db: AppDatabase? = null
    private var ReminderDao: ReminderDao? = null

    private var markerArray: ArrayList<Marker> = ArrayList<Marker>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //onMapReady(map)

        val mapFragment: SupportMapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment;
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this)

        val aTR: View = findViewById(R.id.map_create)
        aTR.setOnClickListener { view ->
            val mapReminderText: TextView = findViewById(R.id.reminder_message)
            val tRTString: String = mapReminderText.text.toString()

            db = AppDatabase.getAppDataBase(context = this)
            ReminderDao = db?.reminderDao()

            for (marker in markerArray) {
                val position: LatLng = marker.position
                val pString: String = position.toString()

                Observable.fromCallable {
                    var newTimeReminder = Reminder(null, location = pString, time = 0, message = tRTString)

                    with(ReminderDao) {
                        this?.insertAll(newTimeReminder)
                    }
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()

                val newMainActivity = Intent(this, MainActivity::class.java)
                finish()
                startActivity(newMainActivity)

                var queryDatabase = db?.reminderDao()?.getAll()
                val adapter = listAdapter(this, ArrayList(queryDatabase!!))
                adapter.notifyDataSetChanged()

            }

        }

    }

    override fun onMapReady(map: GoogleMap) {
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

//        val myPlace = LatLng(40.73, -73.99)  // this is New York
//        map.addMarker(MarkerOptions().position(myPlace).title("My Favorite City"))
//        map.moveCamera(CameraUpdateFactory.newLatLng(myPlace))

        map.isMyLocationEnabled = true;

        map.setOnMapClickListener {
            //allPoints.add(it)
            map.clear()
            var currMarker: Marker = map.addMarker(MarkerOptions().position(it))

            markerArray.add(currMarker)
        }

        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            var location: Location? = task.result
            if (location == null) {
                requestNewLocationData()
            } else {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), 13F
                    ))

                val cameraPosition: CameraPosition = CameraPosition.Builder()
                    .target(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )      // Sets the center of the map to location user
                    .zoom(17F)                   // Sets the zoom
                    .bearing(90F)                // Sets the orientation of the camera to east
                    .tilt(40F)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        }
    }

    override fun onMarkerClick(p0: Marker?) = false

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        //findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                        //findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
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
            //findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            //findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
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

