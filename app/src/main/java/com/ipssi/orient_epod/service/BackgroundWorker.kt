package com.ipssi.orient_epod.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ipssi.orient_epod.R
import com.ipssi.orient_epod.remote.util.AppConstant

/**
 *  @author Ritesh Kumar
 */
class BackgroundWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("[doWork]","started workmanager")
        val intent =
            Intent(context, LocationScanningService::class.java)

        startService(intent)

        return Result.success()
    }

    private fun startService(intent: Intent) {
        val uniqueId = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE).getString(AppConstant.VEHICLE_NUMBER, "")
            ?: ""
        Log.d("[startService]", "ID=$uniqueId")
        if (!LocationScanningService.serviceRunning && uniqueId.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("[startService]", "startForegroundService")
                context.startForegroundService(intent)
            } else {
                Log.d("[startService]", "startService")
                context.startService(intent)
            }
        }
    }

    companion object {
        val UNIQUE_WORK_NAME = BackgroundWorker::class.java.simpleName
    }
}