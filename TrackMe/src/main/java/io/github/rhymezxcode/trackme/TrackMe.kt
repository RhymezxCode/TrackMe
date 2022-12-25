package io.github.rhymezxcode.trackme

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat


class TrackMe(
    context: Context?,
){
    private val tracker = context?.let { Tracker(it) }

    fun stopUsingTrackMe(): Unit? {
        return tracker?.stopUsingTrackMe()
    }

    fun showSettingsDialog(): Unit? {
        return tracker?.showSettingsDialog()
    }

    fun checkMyLocationStatus(): Boolean? {
        return tracker?.checkMyLocationStatus()
    }

    fun getMyLatitude(): Double? {
        return tracker?.getLatitude()
    }

    fun getMyUpdatedLatitude(): Double? {
        return tracker?.getUpdatedLatitude()
    }

    fun getMyLongitude(): Double? {
        return tracker?.getLongitude()
    }

    fun getMyUpdatedLongitude(): Double? {
        return tracker?.getUpdatedLongitude()
    }

    fun getMyLocation(): Location? {
        return tracker?.getLocation()
    }

    fun getMyUpdatedLocation(): Location? {
        return tracker?.getUpdatedLocation()
    }

    private constructor(builder: Builder) : this(
        builder.context
    )

    class Builder {
        var context: Context? = null
            private set

        fun context(context: Context) = apply { this.context = context }

        fun build() = TrackMe(this)
    }

    internal class Tracker(private val mContext: Context) : Service(), LocationListener {

        // flag for Location status
        private var checkLocationStatus = false
        private var location: Location? = null
        private var latitude: Double? = 0.0
        private var longitude: Double? = 0.0
        private var updatedLatitude: Double? = 0.0
        private var updatedLongitude: Double? = 0.0
        private var updatedLocation: Location? = null

        // Declaring a Location Manager
        private var locationManager: LocationManager? = null

        init {
            fetchLocation()
        }

        private fun fetchLocation() {
            try {
                locationManager = mContext
                    .getSystemService(LOCATION_SERVICE) as LocationManager
                assert(locationManager != null)
                val isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)
                // getting network status
                val isNetworkEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGPSEnabled || isNetworkEnabled) {
                    if (isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        } // TODO: Consider calling
                        locationManager?.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        Log.d("Network", "Network")
                        if (locationManager != null) {
                            location =
                                locationManager?.getLastKnownLocation(LocationManager
                                    .NETWORK_PROVIDER)
                            if (location != null) {
                                checkLocationStatus = true
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            if (locationManager != null) {
                                locationManager?.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                                )
                            }
                            Log.d("GPS Enabled", "GPS Enabled")
                            if (locationManager != null) {
                                location =
                                    locationManager?.getLastKnownLocation(LocationManager
                                        .GPS_PROVIDER)
                                if (location != null) {
                                    checkLocationStatus = true
                                    latitude = location!!.latitude
                                    longitude = location!!.longitude
                                }
                            }
                        }
                    }
                } else {
                    checkLocationStatus = false
                    // no network provider is enabled
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GPS tracker:", "" + e.printStackTrace())
            }
        }

        /**
         * Stop using GPS listener
         * Calling this function will stop using GPS in your app
         */
        fun stopUsingTrackMe() {
            if (locationManager != null) {
                locationManager?.removeUpdates(this@Tracker)
            }
        }

        /**
         * Function to get latitude
         */
        fun getLatitude(): Double? {
            if (location != null) {
                latitude = location!!.latitude
                return latitude
            }
            // return latitude
            return null
        }

        fun getUpdatedLatitude(): Double? {
            if (updatedLatitude != 0.0) {
                return updatedLatitude
            }
            // return latitude
            return null
        }

        /**
         * Function to get longitude
         */
        fun getLongitude(): Double? {
            if (location != null) {
                longitude = location!!.longitude
                return longitude
            }
            // return longitude
            return null
        }

        fun getUpdatedLongitude(): Double? {
            if (updatedLongitude != 0.0) {
                return updatedLongitude
            }
            // return longitude
            return null
        }

        /**
         * Function to get location
         */
        fun getLocation(): Location? {
            if (location != null) {
                return location
            }
            // return longitude
            return null
        }

        fun getUpdatedLocation(): Location? {
            if (updatedLocation != null) {
                return updatedLocation
            }
            // return longitude
            return null
        }

        /**
         * Function to check if GPS/wifi is enabled
         *
         * @return boolean
         */
        fun checkMyLocationStatus(): Boolean {
            return checkLocationStatus
        }

        /**
         * Function to show settings alert dialog
         * On pressing Settings button it will launch settings
         */
        fun showSettingsDialog() {
            val alertDialog = AlertDialog.Builder(mContext)
            // SettingActivity Dialog Title
            alertDialog.setTitle("Turn your Location on from settings")
            // SettingActivity Dialog Message
            alertDialog.setMessage("Location is not enabled. Do you want to go to settings?")
            // On pressing Settings button
            alertDialog.setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext.startActivity(intent)
            }
            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            // Showing Alert Message
            alertDialog.show()
        }

        override fun onLocationChanged(location: Location) {
            updatedLatitude = location.latitude
            updatedLongitude = location.longitude
            updatedLocation = location
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onBind(intent: Intent): IBinder? {
            // TODO Auto-generated method stub
            return null
        }

        companion object {
            // The minimum distance to change Updates in meters
            private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

            // The minimum time between updates in milliseconds
            private const val MIN_TIME_BW_UPDATES = (1000 * 60 // 1 minute
                    ).toLong()
        }
    }

}