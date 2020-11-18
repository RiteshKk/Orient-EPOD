package com.ipssi.orient_epod.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.ipssi.orient_epod.OrientApp
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.formatTimeStamp
import com.ipssi.orient_epod.model.DriverLocationEntity
import com.ipssi.orient_epod.remote.remote.ApiClient
import com.ipssi.orient_epod.remote.repository.ApiHelper
import com.ipssi.orient_epod.remote.util.AppConstant
import kotlinx.coroutines.*


/**
 * @author Ritesh Kumar
 */

class RetrieveLocationService {
    companion object {
        private const val UPDATE_INTERVAL: Long = 1 * 60 * 1000  /* 1 min */
        private const val FASTEST_INTERVAL: Long = 1 * 60 * 1000 /* 1 min */
        private const val DISPLACEMENT = 10f //1m
    }


    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val context = OrientApp.instance
    private var isServiceRunning = false
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)


    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult?.let {
                if (it.lastLocation != null) {
                    val latitude = locationResult.locations[0].latitude.toString()
                    val longitude = locationResult.locations[0].longitude.toString()
                    val speed = locationResult.locations[0].speed.toString()
                    val recordTime = formatTimeStamp()
                    Log.d(
                            "Retreive location",
                            "$latitude - $longitude"
                    )
                    val pref = context.getSharedPreferences(context.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
                    val shipmentNumber = pref.getString(AppConstant.SHIPMENT_NUMBER, "") ?: ""
                    val driverLocationEntity = DriverLocationEntity(shipment = shipmentNumber, lat = latitude, lng = longitude, recordTime = recordTime, speed = speed)
                    scope.launch {
                        try {
                            ApiHelper(ApiClient.getApiService()).saveDriverLocation(driverLocationEntity)
                        } catch (ex: Exception) {
                            Log.d("RetrieveLocationService", "save Driver location api failed")
                        }
                    }
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