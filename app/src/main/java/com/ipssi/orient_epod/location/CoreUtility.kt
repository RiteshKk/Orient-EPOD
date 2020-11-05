package com.ipssi.orient_epod.location

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ipssi.orient_epod.OrientApp
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.service.BackgroundWorker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Ritesh Kumar
 */
class CoreUtility {

    companion object {
        fun getCurrentEpochTimeInSec(): Int {
            val dateObj = Date()
            return (dateObj.time / 1000).toInt()
        }

        fun requestPermissions(
            context: Activity,
            permissionRequestCode: Int
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    permissionRequestCode
                )
            } else {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    permissionRequestCode
                )
            }
        }

        fun enableLocation(context: Context, locationOn: LocationUtils.TurnLocationListener) {
            LocationUtils(context).turnLocationOn(locationOn)
        }

        /** check if location is On
         * @param context
         * @return true if gps or network is on false otherwise
         * */
        fun isLocationOn(context: Context): Boolean {

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            var gpsEnabled = false
            var networkEnabled = false

            try {
                gpsEnabled =
                    locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            } catch (e: Exception) {
            }

            try {
                networkEnabled =
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
            } catch (e: Exception) {
            }

            return (gpsEnabled || networkEnabled)
        }

        /** check if location permission is granted
         * @param context
         * @return true if permission granted false otherwise*/
        fun isLocationPermissionAvailable(context: Context): Boolean {
            val permission3 =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            val permission4 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

            return (permission3 == PackageManager.PERMISSION_GRANTED || permission4 == PackageManager.PERMISSION_GRANTED)
        }


        fun getNotificationChannel(): String {
            return AppConstant.NOTIFICATION_CHANNEL
        }


        fun startBackgroundWorker() {
            val workManager = WorkManager.getInstance(OrientApp.instance)
            val workRequest = PeriodicWorkRequest.Builder(
                BackgroundWorker::class.java,
                16,
                TimeUnit.MINUTES
            ).build()
            workManager.enqueueUniquePeriodicWork(
                BackgroundWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}




