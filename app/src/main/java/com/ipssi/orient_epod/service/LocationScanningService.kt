package com.ipssi.orient_epod.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ipssi.orient_epod.MainActivity
import com.ipssi.orient_epod.OrientApp
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.location.CoreUtility.Companion.getNotificationChannel
import com.ipssi.orient_epod.location.CoreUtility.Companion.isLocationOn
import com.ipssi.orient_epod.location.LocationAPI
import com.ipssi.orient_epod.remote.util.AppConstant
import java.util.*


/**
 * @author Ritesh Kumar
 */
class LocationScanningService : Service() {
    private var retrieveLocationService: LocationAPI? = null
    private val mData: MutableList<String> =
        ArrayList()
    private var searchTimestamp: Long = 0
    private val TAG = this.javaClass.name
    private var timer: Timer? = null


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification =
            getNotification(AppConstant.NOTIFICATION_DESC)
        startForeground(NOTIF_ID, notification)
        searchTimestamp = System.currentTimeMillis()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channelId = getNotificationChannel()
            val channel = NotificationChannel(channelId, name, importance)
            channel.enableLights(false)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            channel.description = AppConstant.NOTIFICATION_DESC
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        serviceRunning = true
        configureNotification()

        startLocationUpdate()

        registerLocationStateListener()
        Log.d(TAG, "onStartCommand service started")
        return START_STICKY
    }

    private fun configureNotification() {
        val notification: Notification = if (!isLocationOn(OrientApp.instance.applicationContext)) {
            getNotification(AppConstant.PLEASE_ALLOW_LOCATION)
        } else {
            getNotification(AppConstant.NOTIFICATION_DESC)
        }
        startForeground(NOTIF_ID, notification)
    }

    private fun getNotification(notificationDescText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivities(
            this,
            0,
            arrayOf(notificationIntent),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getNotificationChannel() else ""
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId)
        val bigTextStyle =
            NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(resources.getString(R.string.app_name))
        bigTextStyle.bigText(notificationDescText)
        return notificationBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(notificationDescText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSound(null)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setColor(resources.getColor(R.color.colorPrimary))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun startLocationUpdate() {
        /* retrieveLocationService = LocationAPI(applicationContext,null)
         retrieveLocationService!!.onStart()*/
    }

    override fun onBind(intent: Intent): Binder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        serviceRunning = false
        try {
            mLocationChangeListener?.let { unregisterReceiver(it) }
            stopForeground(true)
            if (retrieveLocationService != null) {
//                retrieveLocationService!!.stopService()
            }

            if (timer != null) {
                timer!!.cancel()
            }

        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun registerLocationStateListener() {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(mLocationChangeListener, filter)
    }

    private val mLocationChangeListener: BroadcastReceiver? =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                val action = intent.action
                val notification: Notification
                if (LocationManager.PROVIDERS_CHANGED_ACTION == action) {
                    if (!isLocationOn(OrientApp.instance.applicationContext)) {
                        notification =
                            getNotification(AppConstant.PLEASE_ALLOW_LOCATION)
                        updateNotification(notification)
                    }
                }
            }
        }

    private fun updateNotification(notification: Notification) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, notification)
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
        stopSelf()
        serviceRunning = false
    }


    companion object {
        private const val NOTIF_ID = 1973
        var serviceRunning = false
    }
}