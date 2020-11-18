package com.ipssi.orient_epod.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ipssi.orient_epod.configureService
import com.ipssi.orient_epod.location.CoreUtility

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, bootIntent: Intent?) {
        Log.d(BootReceiver::class.java.simpleName,"${bootIntent?.action} action found")
        if (context == null) return
        if (bootIntent?.action == Intent.ACTION_BOOT_COMPLETED) {
            startService(context)
        }
    }

    private fun startService(context: Context) {
        if (CoreUtility.isLocationPermissionAvailable(context)) {
            configureService(context)
            CoreUtility.startBackgroundWorker()
        }
    }
}