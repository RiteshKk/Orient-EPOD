package com.ipssi.orient_epod.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ipssi.orient_epod.OrientApp
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.service.BackgroundWorker
import java.util.concurrent.TimeUnit

/**
 * @author Ritesh Kumar
 */
class CoreUtility {

    companion object {

        fun requestPermissions(
                context: Fragment,
                permissionRequestCode: Int
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.requestPermissions(
                        arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        permissionRequestCode
                )
            } else {
                context.requestPermissions(
                        arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        permissionRequestCode
                )
            }
        }

        fun enableLocation(context: Activity, locationOn: LocationUtils.TurnLocationListener) {
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
                        locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                                ?: false
            } catch (e: Exception) {
            }

            return (gpsEnabled || networkEnabled)
        }

        /** check if location permission is granted
         * @param context
         * @return true if permission granted false otherwise*/
        fun isLocationPermissionAvailable(context: Context): Boolean {

            val permission4 =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

            return (permission4 == PackageManager.PERMISSION_GRANTED)
        }


        fun getNotificationChannel(): String {
            return AppConstant.NOTIFICATION_CHANNEL
        }


        fun startBackgroundWorker() {
            Log.d("[startBackgroundWorker]", "starting workmanager")
            val workManager = WorkManager.getInstance(OrientApp.instance)
            val constraints: Constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true).build()
            val workRequest = PeriodicWorkRequest.Builder(
                    BackgroundWorker::class.java,
                    20,
                    TimeUnit.MINUTES
            )
                    .addTag("periodicWorkRequest")
                    .setConstraints(constraints)
                    .build()
            workManager.enqueue(workRequest)
            /* workManager.enqueueUniquePeriodicWork(
                     BackgroundWorker.UNIQUE_WORK_NAME,
                     ExistingPeriodicWorkPolicy.REPLACE,
                     workRequest
             )*/
        }
    }
}




