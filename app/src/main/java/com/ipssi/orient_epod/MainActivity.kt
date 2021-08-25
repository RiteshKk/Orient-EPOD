package com.ipssi.orient_epod

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ipssi.orient_epod.callbacks.OnLoginListener
import com.ipssi.orient_epod.databinding.ActivityMainBinding
import com.ipssi.orient_epod.login.LoginFragment
import com.ipssi.orient_epod.login.OTPFragment
import com.ipssi.orient_epod.remote.util.AppConstant

class MainActivity : AppCompatActivity(), OnLoginListener {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        val preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val aBoolean = preferences.getBoolean(AppConstant.IS_LOGIN, false)
        if (aBoolean) {
            supportActionBar!!.show()
            replaceFragment(HomeFragment::class.java.name, "home")
        } else {
            supportActionBar!!.hide()
            replaceFragment(LoginFragment::class.java.name, "login")
        }
    }

    private fun replaceFragment(fName: String, tag: String, otp: String?=null) {
        val fragmentManager = supportFragmentManager
        var frag = fragmentManager.findFragmentByTag(tag)
        if (frag == null) {
            frag = supportFragmentManager.fragmentFactory.instantiate(classLoader, fName)
        }
        fragmentManager.beginTransaction().replace(R.id.container, frag, tag)
                .commitAllowingStateLoss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_logout) {
            logout(this)
        } else if (item.itemId == R.id.menu_change_language) {
            val intent = Intent(this, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoginSuccess() {
        supportActionBar!!.show()
        replaceFragment(HomeFragment::class.java.name, "home")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentByTag("home")
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun showOTPView() {
        replaceFragment(OTPFragment::class.java.name, "OTP")
    }
}