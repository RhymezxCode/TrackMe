<div align="center">
<h1>TrackMe Android Library</h1>

<a href="https://android-arsenal.com/api?level=21" target="blank">
    <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" alt="TrackMe Android Library least API level" />
</a>
<a href="https://jitpack.io/#RhymezxCode/TrackMe" target="blank">
    <img src="https://jitpack.io/v/RhymezxCode/TrackMe.svg" alt="TrackMe Android Library on jitpack.io" />
</a>
<a href="https://github.com/RhymezxCode/TrackMe/blob/main/LICENSE" target="blank">
    <img src="https://img.shields.io/github/license/RhymezxCode/TrackMe" alt="TrackMe Android Library License." />
</a>
<a href="https://github.com/RhymezxCode/TrackMe/stargazers" target="blank">
    <img src="https://img.shields.io/github/stars/RhymezxCode/TrackMe" alt="TrackMe Android Library Stars"/>
</a>
<a href="https://github.com/RhymezxCode/TrackMe/fork" target="blank">
    <img src="https://img.shields.io/github/forks/RhymezxCode/TrackMe" alt="TrackMe Android Library Forks"/>
</a>
<a href="https://github.com/RhymezxCode/TrackMe/issues" target="blank">
    <img src="https://img.shields.io/github/issues/RhymezxCode/TrackMe" alt="TrackMe Android Library Issues"/>
</a>
<a href="https://github.com/RhymezxCode/TrackMe/commits?author=RhymezxCode" target="blank">
    <img src="https://img.shields.io/github/last-commit/RhymezxCode/TrackMe" alt="TrackMe Android Library Issues"/>
</a>
</div>
<br />

## TrackMe Android Library
An android library used to track your current location, it provides the latitude, longitude and location for your personal use.
<pre>
<img src="/media/demo_track_me.gif" width="250" height="500"/>            <img src="/media/track_me_screenshot.png" width="300" height="600"/>
</pre>


### 1. Adding TrackMe to your project

* Include jitpack in your root `settings.gradle` file.

```gradle
pluginManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

* And add it's dependency to your app level `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.RhymezxCode:TrackMe:1.0.1'

    //Dexter for runtime permissions
    implementation 'com.karumi:dexter:6.2.3'
}
```

#### Sync your project, and :scream: boom :fire: you have added TrackMe successfully. :exclamation:

### 2. Usage

* First initialize the builder class:

```kt
        val trackMe = TrackMe.Builder()
            .context(context = this)
            .build()
```

* make sure you've accepted permissions for ACCESS_FINE_LOCATION, before checking your location status and using TrackMe to get your location.

```kt

//Using Dexter for runtime permissions
  private fun permissionCheck() {
        Dexter.withContext(this@MainActivity)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if(p0?.areAllPermissionsGranted() == true){
                         lifecycleScope.launch {
                             getCurrentLocation()
                         }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

   private suspend fun getCurrentLocation() {
        when (trackMe?.checkMyLocationStatus()) {
            true -> {
                try {
                    val addresses: List<Address>? = withContext(Dispatchers.IO){
                        geoCoder.getFromLocation(
                            trackMe?.getMyLatitude() ?: 0.0,
                            trackMe?.getMyLongitude() ?: 0.0,
                            1
                        )
                    }

                    if (!addresses.isNullOrEmpty()) {
                        Log.e("Your Full Address: ", "$addresses")

                        val address: String = addresses[0].getAddressLine(0)

                        val currentLatitude = trackMe?.getMyLatitude() ?: 0.0
                        val currentLongitude = trackMe?.getMyLongitude() ?: 0.0
                        val currentAddress = address

                        val currentLatLng = LatLng(
                            currentLatitude,
                            currentLongitude
                        )

                        val marker: MarkerOptions = MarkerOptions()
                            .position(currentLatLng)
                            .title(currentAddress)

                        lifecycleScope.launchWhenStarted {
                        //Using your location to pin a marker on the map
                            map.addMarker(marker)

                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    17f
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            false -> {
                lifecycleScope.launchWhenStarted {
                
                    showToast(
                        this@MainActivity,
                        "Your location is not available yet!"
                    )
                }

            }

            else -> {}
        }

    }
```
* Available methods for your location:

```kt                            
                                 //DataTypes
      trackMe?.getMyLatitude()   //Double
      trackMe?.getMyLongitude()  //Double
      trackMe?.getMyLocation()   //Location
```
Note: You can run the sample project in the repo, to see how it works!

:pushpin: Please, feel free to give me a star :star2:, I also love sparkles :sparkles: :relaxed:
<div align="center">
    <sub>Developed with :sparkling_heart: by
        <a href="https://github.com/RhymezxCode">Awodire Babajide Samuel</a>
    </sub>
</div>


