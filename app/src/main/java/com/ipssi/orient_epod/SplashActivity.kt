package com.ipssi.orient_epod

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ipssi.orient_epod.remote.util.AppConstant

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val isFirstLaunch = preferences.getBoolean(AppConstant.IS_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, LanguageSelectorActivity::class.java))
                finish()
                preferences.edit().putBoolean(AppConstant.IS_FIRST_LAUNCH, false).apply()
            }, 3000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 3000)
        }
    }
}