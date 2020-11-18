package com.ipssi.orient_epod

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.ipssi.orient_epod.location.CoreUtility
import java.util.concurrent.Executors

class OrientApp : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newFixedThreadPool(8))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        WorkManager.initialize(
            this,
            workManagerConfiguration
        )
    }

    companion object {
        lateinit var instance: OrientApp
    }
}