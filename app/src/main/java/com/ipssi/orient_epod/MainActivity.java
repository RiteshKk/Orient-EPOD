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
import com.ipssi.orient_epod.login.OTPFragment;
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
            UtilKt.logout(this);
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
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void showOTPView() {
        replaceFragment(OTPFragment.class.getName(), "OTP");
    }
}