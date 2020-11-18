package com.ipssi.orient_epod

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.ipssi.orient_epod.remote.util.AppConstant
import com.ipssi.orient_epod.service.LocationScanningService
import java.text.SimpleDateFormat
import java.util.*

fun showAlertDialog(activity: Activity, message: String?) {
    val builder = AlertDialog.Builder(activity)
    builder.setMessage(message
            ?: AppConstant.GENERIC_ERROR).setTitle("Alert").setPositiveButton("ok") { dialog: DialogInterface, _: Int ->
        dialog.dismiss()
    }.show()
}


fun hideKeyboard(view: View, context: Context) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun formatTimeStamp(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
    return formatter.format(Date())
}

fun configureService(context: Context) {
    val preference = context.getSharedPreferences(context.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
    val uniqueId = preference.getString(AppConstant.TRANSPORTER_CODE, "") ?: ""
    Log.d("[Util configureService]", "${uniqueId}")
    if (!LocationScanningService.serviceRunning && uniqueId.isNotEmpty()) {
        Log.d("[Util configureService]", "starting service")
        val intent = Intent(context, LocationScanningService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}