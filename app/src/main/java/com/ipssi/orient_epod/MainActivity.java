package com.ipssi.orient_epod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ipssi.orient_epod.callbacks.OnLoginListener;
import com.ipssi.orient_epod.databinding.ActivityMainBinding;
import com.ipssi.orient_epod.location.CoreUtility;
import com.ipssi.orient_epod.login.LoginFragment;
import com.ipssi.orient_epod.remote.util.AppConstant;
import com.ipssi.orient_epod.service.LocationScanningService;

public class MainActivity extends AppCompatActivity implements OnLoginListener {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        boolean aBoolean = preferences.getBoolean(AppConstant.IS_LOGIN, false);
        if (aBoolean) {
            getSupportActionBar().show();
            replaceFragment(HomeFragment.class.getName(), "home");
        } else {
            getSupportActionBar().hide();
            replaceFragment(LoginFragment.class.getName(), "login");
        }
    }

    private void replaceFragment(String fname, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment frag = fragmentManager.findFragmentByTag(tag);
        if (frag == null) {
            frag = fragmentManager.getFragmentFactory().instantiate(getClassLoader(), fname);
        }
        fragmentManager.beginTransaction().replace(R.id.container, frag, tag)
                .commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            stopService(new Intent(this, LocationScanningService.class));
            CoreUtility.Companion.cancelBackgroundWorker();
            preferences.edit().putString(AppConstant.TRANSPORTER_CODE, null)
                    .putString(AppConstant.VEHICLE_NUMBER, null)
                    .putString(AppConstant.SHIPMENT_NUMBER, null)
                    .putBoolean(AppConstant.IS_LOGIN, false)
                    .apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_change_language) {
            Intent intent = new Intent(this, LanguageSelectorActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginSuccess() {
        getSupportActionBar().show();
        replaceFragment(HomeFragment.class.getName(), "home");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("home");
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}