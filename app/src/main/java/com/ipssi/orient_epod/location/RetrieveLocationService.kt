package com.ipssi.orient_epod.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.ipssi.orient_epod.OrientApp


/**
 * @author Ritesh Kumar
 */

class RetrieveLocationService {
    companion object {
        private const val UPDATE_INTERVAL: Long = 1 * 60 * 1000  /* 1 min */
        private const val FASTEST_INTERVAL: Long = 1 * 60 * 1000 /* 1 min */
        private const val DISPLACEMENT = 1f //1m
    }


    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val context = OrientApp.instance
    private var isServiceRunning = false


    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult?.let {
                if (it.lastLocation != null) {
                    Toast.makeText(context,locationResult.locations[0].latitude.toString() + " - " + locationResult.locations[0].longitude.toString(),Toast.LENGTH_SHORT).show()
                    Log.d(
                        "Retreive location",
                        locationResult.locations[0].latitude.toString() + " - " + locationResult.locations[0].longitude.toString()
                    )
                }
            }

        }
    }


    fun startService() {
        if (isServiceRunning) {
            return
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        getLocation()
    }


    private fun getLocation() {
        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        val mLocationRequestHighAccuracy = LocationRequest()
        mLocationRequestHighAccuracy.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        mLocationRequestHighAccuracy.interval = UPDATE_INTERVAL
        mLocationRequestHighAccuracy.fastestInterval = FASTEST_INTERVAL
        mLocationRequestHighAccuracy.smallestDisplacement = DISPLACEMENT



        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        mFusedLocationClient.requestLocationUpdates(
            mLocationRequestHighAccuracy, locationCallback,
            Looper.myLooper()
        )

        isServiceRunning = true
    }

    fun stopService() {
        if (isServiceRunning) {
            mFusedLocationClient.removeLocationUpdates(locationCallback).addOnSuccessListener {
                isServiceRunning = false
            }
        }
    }

}