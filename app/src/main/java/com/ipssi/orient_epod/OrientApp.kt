package com.ipssi.orient_epod

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import java.util.concurrent.Executors

class OrientApp : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setExecutor(Executors.newFixedThreadPool(8)).build()
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