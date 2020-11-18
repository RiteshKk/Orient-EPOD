package com.ipssi.orient_epod;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.ipssi.orient_epod.adapter.LanguageSelectorAdapter;
import com.ipssi.orient_epod.callbacks.OnLanguageSelectedListener;
import com.ipssi.orient_epod.login.LoginFragment;
import com.ipssi.orient_epod.model.LanguageDetail;
import com.ipssi.orient_epod.remote.util.AppConstant;

import java.util.ArrayList;
import java.util.Locale;

public class LanguageSelectorActivity extends AppCompatActivity implements OnLanguageSelectedListener {


    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selector);
        preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        if (preferences.getBoolean(AppConstant.IS_FIRST_LAUNCH, true)) {
            showPermissionDialog();
        }
        RecyclerView recyclerView = findViewById(R.id.language_list);
        findViewById(R.id.btn_get_started).setOnClickListener(v -> {
            preferences.edit().putBoolean(AppConstant.IS_FIRST_LAUNCH, false).apply();
            Intent intent = new Intent(LanguageSelectorActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        ArrayList<LanguageDetail> languageList = createLanguageList();
        String selectedLang = preferences.getString(AppConstant.SELECTED_LANGUAGE, "English");
        recyclerView.setAdapter(new LanguageSelectorAdapter(this, languageList, selectedLang));
    }

    private ArrayList<LanguageDetail> createLanguageList() {
        ArrayList<LanguageDetail> languageDetailList = new ArrayList<>();
        languageDetailList.add(new LanguageDetail("en", "English", "English"));
        languageDetailList.add(new LanguageDetail("hi", "हिन्दी", "Hindi"));
        languageDetailList.add(new LanguageDetail("te", "తెలుగు", "Telugu"));
        languageDetailList.add(new LanguageDetail("kn", "ಕನ್ನಡ", "Kannada"));
        languageDetailList.add(new LanguageDetail("mr", "मराठी", "Marathi"));
        languageDetailList.add(new LanguageDetail("ta", "தமிழ்", "Tamil"));

        return languageDetailList;
    }

    public void setLocale(String langCode) {
        Locale locale;
        locale = new Locale(langCode);
        Configuration config = new Configuration(getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config,
            getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onLanguageSelected(LanguageDetail details) {
        Log.d("selected language", details.getLangEnglishName());
        preferences.edit().putString(AppConstant.SELECTED_LANGUAGE, details.getLangName()).apply();
        setLocale(details.getLangCode());
    }

    public void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.required_permission_title)
            .setMessage(Html.fromHtml(getString(R.string.required_permission_message)))
            .setCancelable(true)
            .setPositiveButton(R.string.accept, (dialog, which) -> dialog.dismiss())
            .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss()).show();
    }
}