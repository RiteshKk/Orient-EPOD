package com.ipssi.orient_epod;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.appbar.AppBarLayout;
import com.ipssi.orient_epod.databinding.ActivityInvoiceDetailsBinding;
import com.ipssi.orient_epod.model.Invoice;
import com.ipssi.orient_epod.remote.util.AppConstant;
import com.ipssi.orient_epod.ui.main.SectionsPagerAdapter;

public class InvoiceDetailsActivity extends AppCompatActivity {
    private ActivityInvoiceDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice_details);
        setSupportActionBar(binding.toolbar);
        binding.setLifecycleOwner(this);
        Invoice model = (Invoice) getIntent().getParcelableExtra(AppConstant.MODEL);
        binding.setModel(model);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),model);
        binding.viewPager.setAdapter(sectionsPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}