package com.ipssi.orient_epod

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ipssi.orient_epod.adapter.LanguageSelectorAdapter
import com.ipssi.orient_epod.callbacks.OnLanguageSelectedListener
import com.ipssi.orient_epod.model.LanguageDetail
import com.ipssi.orient_epod.remote.util.AppConstant
import java.util.*

class LanguageSelectorActivity : AppCompatActivity(), OnLanguageSelectedListener {
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selector)
        preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        if (preferences.getBoolean(AppConstant.IS_FIRST_LAUNCH, true)) {
            showPermissionDialog()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.language_list)
        findViewById<View>(R.id.btn_get_started).setOnClickListener {
            preferences.edit().putBoolean(AppConstant.IS_FIRST_LAUNCH, false).apply()
            val intent = Intent(this@LanguageSelectorActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        val languageList = createLanguageList()
        val selectedLang = preferences.getString(AppConstant.SELECTED_LANGUAGE, "English")
        recyclerView.adapter = LanguageSelectorAdapter(this, languageList, selectedLang)
    }

    private fun createLanguageList(): ArrayList<LanguageDetail> {
        val languageDetailList = ArrayList<LanguageDetail>()
        languageDetailList.add(LanguageDetail("en", "English", "English"))
        languageDetailList.add(LanguageDetail("hi", "हिन्दी", "Hindi"))
        languageDetailList.add(LanguageDetail("te", "తెలుగు", "Telugu"))
        languageDetailList.add(LanguageDetail("kn", "ಕನ್ನಡ", "Kannada"))
        languageDetailList.add(LanguageDetail("mr", "मराठी", "Marathi"))
        languageDetailList.add(LanguageDetail("ta", "தமிழ்", "Tamil"))
        return languageDetailList
    }

    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        val config = resources.configuration
        Locale.setDefault(locale)
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config,
                baseContext.resources.displayMetrics)
    }

    override fun onLanguageSelected(details: LanguageDetail) {
        Log.d("selected language", details.langEnglishName)
        preferences.edit().putString(AppConstant.SELECTED_LANGUAGE, details.langName).apply()
        setLocale(details.langCode)
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.required_permission_title)
                .setMessage(Html.fromHtml(getString(R.string.required_permission_message)))
                .setCancelable(true)
                .setPositiveButton(R.string.accept) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .setNegativeButton(R.string.close) { dialog: DialogInterface, _: Int -> dialog.dismiss() }.show()
    }
}