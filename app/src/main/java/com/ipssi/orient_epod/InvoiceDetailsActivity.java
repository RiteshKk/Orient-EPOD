package com.ipssi.orient_epod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.downloader.core.Core;
import com.google.android.material.appbar.AppBarLayout;
import com.ipssi.orient_epod.databinding.ActivityInvoiceDetailsBinding;
import com.ipssi.orient_epod.location.CoreUtility;
import com.ipssi.orient_epod.model.Invoice;
import com.ipssi.orient_epod.model.Receiver;
import com.ipssi.orient_epod.remote.util.AppConstant;
import com.ipssi.orient_epod.ui.main.PlaceholderFragment;
import com.ipssi.orient_epod.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.ipssi.orient_epod.UtilKt.showAlertDialog;

public class InvoiceDetailsActivity extends AppCompatActivity {
    private ActivityInvoiceDetailsBinding binding;
    private InvoiceDetailsViewModel viewModel;

    public static int totalDamage = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        totalDamage = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice_details);
        viewModel = new ViewModelProvider(this).get(InvoiceDetailsViewModel.class);

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.setLifecycleOwner(this);
        Invoice model = (Invoice) getIntent().getParcelableExtra(AppConstant.MODEL);
        StringBuilder address = new StringBuilder();
        if (model.getShiptopartyAddress() != null && !model.getShiptopartyAddress().isEmpty()) {
            address.append(model.getShiptopartyAddress());
        }
        if (model.getShiptopartyAddress1() != null && !model.getShiptopartyAddress1().isEmpty()) {
            address.append(address.length() > 0 ? "\n" : "");
            address.append(model.getShiptopartyAddress1());
        }
        if (model.getShiptopartyAddress2() != null && !model.getShiptopartyAddress2().isEmpty()) {
            address.append(address.length() > 0 ? "\n" : "");
            address.append(model.getShiptopartyAddress2());
        }
        model.setShiptopartyAddress(address.toString());
        binding.setModel(model);

        viewModel.getReceivers(model.getInvoiceNumber());

        setObserver();


        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    binding.toolbarTitle.setVisibility(View.VISIBLE);
                } else if (isShow) {
                    isShow = false;
                    binding.toolbarTitle.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setObserver() {
        viewModel.getReceiversList().observe(this, (receivers) -> {
            switch (receivers.getStatus()) {
                case SUCCESS:
                    ArrayList<Receiver> data = receivers.getData();
                    Invoice model = (Invoice) getIntent().getParcelableExtra(AppConstant.MODEL);
                    ArrayList<PlaceholderFragment> fragments = new ArrayList<>();
                    for (int i = 0; data != null && i < data.size(); i++) {
                        if (i < 3) {
                            Receiver receiverModel = data.get(i);
                            totalDamage += Integer.parseInt(receiverModel.getShortage());
                        }
                    }
                    fragments.add(PlaceholderFragment.newInstance(model, (data != null && data.size() > 0) ? data.get(0) : null));
                    fragments.add(PlaceholderFragment.newInstance(model, (data != null && data.size() > 1) ? data.get(1) : null));
                    fragments.add(PlaceholderFragment.newInstance(model, (data != null && data.size() > 2) ? data.get(2) : null));
                    SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fragments);
                    binding.viewPager.setAdapter(sectionsPagerAdapter);
                    binding.tabLayout.setupWithViewPager(binding.viewPager);
                    binding.loadingLayout.setVisibility(View.GONE);
                    break;
                case ERROR:
                case OFFLINE:
                    binding.loadingLayout.setVisibility(View.GONE);
                    showAlertDialog(this, receivers.getMessage());
                    break;
                case LOADING:
                    binding.loadingLayout.setVisibility(View.VISIBLE);
                    break;
            }

        });
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
            preferences.edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.menu_change_language) {
            Intent intent = new Intent(this, LanguageSelectorActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}