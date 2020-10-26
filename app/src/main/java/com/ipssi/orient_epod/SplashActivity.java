package com.ipssi.orient_epod;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.ipssi.orient_epod.login.LoginFragment;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        boolean isFirstLaunch = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).getBoolean("isFirstLaunch", true);
        if(isFirstLaunch) {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, LanguageSelectorActivity.class));
                finish();
            }, 3000);
        }else{
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }, 3000);
        }
    }
}