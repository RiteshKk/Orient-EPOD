package com.ipssi.orient_epod.location

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class LocationUtils(private val context: Context) {
    fun turnLocationOn(turnLocationListener: TurnLocationListener) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val mLocationSettingsRequest = builder.build()
        val client = LocationServices.getSettingsClient(context)
        val task =
            client.checkLocationSettings(mLocationSettingsRequest)
        task.addOnSuccessListener(
            (context as Activity)
        ) {
            turnLocationListener.locationStatus(true)
        }
        task.addOnFailureListener(
            context
        ) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        context,
                        LOCATION_REQUEST
                    )
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
                    Toast.makeText(context, "Someting went wrong", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    interface TurnLocationListener {
        fun locationStatus(isTurnOn: Boolean)
    }

    companion object {
        const val LOCATION_REQUEST = 1245
    }

}