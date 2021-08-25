package com.ipssi.orient_epod.location

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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


//        fun getNotificationChannel(): String {
//            return AppConstant.NOTIFICATION_CHANNEL
//        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getNotificationChannel(): String {
            val channelId = AppConstant.NOTIFICATION_CHANNEL
            val channelName = "My Background Service"
            val chan = NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH)
            chan.lightColor = Color.BLUE
            chan.importance = NotificationManager.IMPORTANCE_HIGH
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = OrientApp.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            return channelId
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
            workManager.enqueueUniquePeriodicWork(
                    BackgroundWorker.UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
            )
        }

        fun cancelBackgroundWorker() {
            Log.d("[cancelWorker]", "cancelling workmanager")
            val workManager = WorkManager.getInstance(OrientApp.instance)
            workManager.cancelAllWorkByTag("periodicWorkRequest")
        }

        fun checkPermission(context: Activity, permission: String?): Boolean {
            return if (Build.VERSION.SDK_INT >= 23) {
                val result = ContextCompat.checkSelfPermission(context, permission!!)
                result == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun requestPermission(activity: Activity, permission: String, PERMISSION_REQUEST_CODE: Int) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show()
            }
            ActivityCompat.requestPermissions(activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }
}




