package com.ipssi.orient_epod.ui.main;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ipssi.orient_epod.model.Invoice;
import com.ipssi.orient_epod.model.Receiver;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {


    private Invoice mModel;

    public SectionsPagerAdapter(FragmentManager fm, Invoice model) {
        super(fm);
        mModel = model;

    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
//        boolean isEditable = false;
       /* Receiver receiver = mModel.getReceivers().get(position);

        if (receiver == null && position == 0) {
            isEditable = true;
        } else if (position > 0 && receiver == null && mModel.getReceivers().get(position - 1) != null) {
            isEditable = true;
        }*/
        return PlaceholderFragment.newInstance(mModel.getInvoiceNumber());
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "Receiver#" + (position + 1);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
//        return mModel.getReceiversCount();
        return 3;
    }
}