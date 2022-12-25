package io.github.rhymezxcode.TrackMeExample

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG)
        .show()
}

fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
    val googlePlayService = GoogleApiAvailability.getInstance()
    val status = googlePlayService.isGooglePlayServicesAvailable(activity)
    if (status != ConnectionResult.SUCCESS) {
        if (googlePlayService.isUserResolvableError(status)) {
            googlePlayService.getErrorDialog(activity, status, 2404)?.show()
        }
        return false
    }
    return true
}

fun closeKeyboard(activity: Activity?) {
    activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}