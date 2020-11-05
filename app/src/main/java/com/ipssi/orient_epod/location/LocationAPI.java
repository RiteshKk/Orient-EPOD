package com.ipssi.orient_epod.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;

/**
 * Created by ritesh on 1/11/2020.
 */

public class LocationAPI {
    public static final String TAG = "LocationApi";
    private static final int REQUEST_CHECK_SETTINGS = 101;

    public final ProgressDialog dialog;
    private LocationRequest mLocationRequest;
    private WeakReference<Activity> contextHolder;
    private OnLocationChangeCallBack listener;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS ;


    public LocationAPI(Activity context, OnLocationChangeCallBack listener) {
        contextHolder = new WeakReference<>(context);
        this.listener = listener;
        dialog = new ProgressDialog(contextHolder.get());
        dialog.setMessage("Getting Location Updates\nPlease Wait...");
        dialog.setCancelable(false);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationCallback();
        createLocationRequest();

    }


    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnSuccessListener(aVoid -> Log.d("locationUpdate","removed successfully"));
    }

    public void onStart() {
        buildLocationSettingsRequest();
        dialog.show();
        startLocationUpdates();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(dialog!=null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                for (Location loc : locationResult.getLocations()) {
                    if(loc!=null && loc.getLatitude() > 0) {
                        Log.e("onLocationResult", "loc= " + loc.toString());
                        listener.onLocationChange(loc);
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
    }


    public void onStop() {
        stopLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder settingRequest = new LocationSettingsRequest.Builder();
        settingRequest.addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(contextHolder.get()).checkLocationSettings(settingRequest.build());
        task.addOnSuccessListener(locationSettingsResponse -> {
            // start Location updates here
        });

        task.addOnCanceledListener(() -> {
            if(dialog!=null && dialog.isShowing()) {
                Log.d("onCancel", "cancel Location updates");
                dialog.dismiss();
            }
        });

        task.addOnFailureListener(e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    if(dialog!=null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                            "location settings ");
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(contextHolder.get(), REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    if(dialog!=null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    String errorMessage = "Location settings are inadequate, and cannot be " +
                            "fixed here. Fix in Settings.";
                    Log.e(TAG, errorMessage);
                    Toast.makeText(contextHolder.get(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());
    }
}


