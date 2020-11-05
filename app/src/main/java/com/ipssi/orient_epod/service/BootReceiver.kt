package com.ipssi.orient_epod.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.configureService
import com.ipssi.orient_epod.location.CoreUtility
import com.ipssi.orient_epod.remote.util.AppConstant

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, bootIntent: Intent?) {
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