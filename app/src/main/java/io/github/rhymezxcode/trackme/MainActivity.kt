@file:Suppress("DEPRECATION")

package io.github.rhymezxcode.trackme

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.github.rhymezxcode.networkstateobserver.network.NetworkStateObserver
import io.github.rhymezxcode.networkstateobserver.network.Reachability
import io.github.rhymezxcode.trackme.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityMainBinding? = null

    private lateinit var geoCoder: Geocoder

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var fromLatitude = 0.0
    private var fromLongitude = 0.0
    private var fromCurrentAddress: String? = null
    private var toLatitude = 0.0
    private var toLongitude = 0.0
    private var toCurrentAddress: String? = null

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val dataProvider = DataProvider()
    private lateinit var mapJob: Job
    private lateinit var gpsJob: Job

    private val NETWORK_TEXT = "No Internet Connection"

    private var networkStateObserver: NetworkStateObserver? = null
    private var trackMe: TrackMe? = null

    private fun callNetworkConnection() {
        networkStateObserver?.callNetworkConnection()?.observe(this) { isConnected ->
            lifecycleScope.launch(Dispatchers.IO) {
                if (isConnected) {
                    when {
                        Reachability.hasInternetConnected(
                            this@MainActivity
                        ) ->
                            lifecycleScope.launchWhenStarted {
                                loadGPS()
                            }

                        else -> lifecycleScope.launchWhenStarted {
                            Handler(Looper.getMainLooper()).postDelayed({
                                lifecycleScope.launch(Dispatchers.IO) {
                                    if (!Reachability.hasInternetConnected(
                                            this@MainActivity
                                        )
                                    ) {
                                        lifecycleScope.launchWhenStarted {
                                            showToast(
                                                this@MainActivity,
                                                NETWORK_TEXT
                                            )
                                        }
                                    }
                                }
                            }, 5000)
                        }
                    }
                } else {
                    lifecycleScope.launchWhenStarted {
                        showToast(
                            this@MainActivity,
                            NETWORK_TEXT
                        )
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mapFragment = (supportFragmentManager.findFragmentById(binding?.map?.id!!)
                as SupportMapFragment?)!!

        with(mapFragment) {
            onCreate(savedInstanceState)
            getMapAsync {
                map = it
                loadGPS()
                loadMap(it)
            }
        }

        isGooglePlayServicesAvailable(this)

        geoCoder = Geocoder(this, Locale.getDefault())

        mapJob = Job()

        gpsJob = Job()

        trackMe = TrackMe.Builder()
            .context(context = this)
            .build()

        networkStateObserver = NetworkStateObserver.Builder()
            .activity(activity = this@MainActivity)
            .build()

    }

    private fun getCurrentLocation() {
        when (trackMe?.checkMyLocationStatus()) {
            true -> {
                try {
                    val addresses: List<Address>? = geoCoder.getFromLocation(
                        trackMe?.getMyLatitude() ?: 0.0,
                        trackMe?.getMyLongitude() ?: 0.0,
                        1
                    )

                    if (!addresses.isNullOrEmpty()) {
                        Log.e("Google places: ", "$addresses")

                        val address: String = addresses[0].getAddressLine(0)

                        fromLatitude = trackMe?.getMyLatitude() ?: 0.0
                        fromLongitude = trackMe?.getMyLongitude() ?: 0.0
                        fromCurrentAddress = address

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            false -> {
                lifecycleScope.launchWhenStarted {
                    trackMe?.showSettingsDialog()
                }

            }

            else -> {
                trackMe?.showSettingsDialog()
            }
        }

    }

    //TODO: Configure properly with markers
    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun GoogleMap.createMapNow() {
        lifecycle.coroutineScope.launchWhenStarted {
            try {

                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //       ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[]
                    // permissions, int[] grantResults) to handle the case where the user
                    // grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@launchWhenStarted
                }
                isMyLocationEnabled = false
                uiSettings.isMyLocationButtonEnabled = false

                val fromLatLong = LatLng(
                    fromLatitude,
                    fromLongitude
                )

                val addresses: List<Address>? = withContext(Dispatchers.Default) {
                    geoCoder.getFromLocation(
                        fromLatitude,
                        fromLongitude,
                        1
                    )
                }
                if (!addresses.isNullOrEmpty()) {
                    val address: String = addresses[0].getAddressLine(0)
                    fromCurrentAddress = address
                }

                initializeMapUiSettings()
                initializeMapTraffic()
                initializeMapType()
                initializeMapViewSettings()

                mapType = GoogleMap.MAP_TYPE_NORMAL

                animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        fromLatLong,
                        15f
                    )
                )

            } catch (ex: java.lang.Exception) {
                Log.e("Error", "" + ex.localizedMessage)
                showToast(this@MainActivity, "Error loading map!")
            }
        }

    }

    private fun initializeMapTraffic() {
        map.isTrafficEnabled = true
    }

    private fun initializeMapType() {
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    private fun initializeMapViewSettings() {
        map.isIndoorEnabled = true
        map.isBuildingsEnabled = false
    }

    private fun initializeMapUiSettings() {
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isTiltGesturesEnabled = true
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
    }

    override fun onStop() {
        super.onStop()
        mapFragment.onStop()
    }

    override fun onStart() {
        super.onStart()
        mapFragment.onStart()
    }

    override fun onResume() {
        super.onResume()
        closeKeyboard(this@MainActivity)
        callNetworkConnection()
        mapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapFragment.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsJob.cancel()
        trackMe?.stopUsingTrackMe()
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }

    private fun loadGPS() = lifecycle.coroutineScope.launch(uiDispatcher + gpsJob) {
        dataProvider.loadGPS()
    }

    private fun loadMap(map: GoogleMap) = lifecycle.coroutineScope.launch(uiDispatcher
            + mapJob) {
        dataProvider.loadMap(map)
    }


    inner class DataProvider(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

        suspend fun loadGPS(): Unit = withContext(dispatcher) {
            getCurrentLocation()
        }

        suspend fun loadMap(map: GoogleMap): Unit = withContext(dispatcher) {
            map.createMapNow()
        }
    }

}