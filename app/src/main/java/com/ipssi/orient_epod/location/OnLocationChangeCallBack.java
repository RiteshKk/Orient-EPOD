package com.ipssi.orient_epod.location;

import android.location.Location;

/**
 * Created by ritesh on 12/8/21.
 */

public interface OnLocationChangeCallBack {
    int ACCESS_FINE_LOCATION_CONSTANT = 10;
    int REQUEST_PERMISSION_SETTING = 12;

    void onLocationChange(Location loc);
}
