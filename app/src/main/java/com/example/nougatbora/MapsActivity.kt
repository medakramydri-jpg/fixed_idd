package com.example.nougatbora

import android.Manifest

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.nougatbora.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.google.maps.android.PolyUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import kotlin.jvm.java


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var adapter: ArrayAdapter<String>
    private val predictionList = mutableListOf<AutocompletePrediction>()
    private var searchMarker: Marker? = null
    private var isFollowingUser = true // flag to control camera behavior
    private var isFirstLocationUpdate = true
    private var currentLatLng: LatLng? = null
    private var currentPolyline: com.google.android.gms.maps.model.Polyline? = null
    private var selectedDestination: LatLng? = null
    private var hasSentArrivalNotification = false
    private val ARRIVAL_DISTANCE_METERS = 1000
    private var selectedPlaceName: String? = null
    private lateinit var sessionToken: AutocompleteSessionToken


    private val LOCATION_PERMISSION_REQUEST = 1001
    private val GPS_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionToken = AutocompleteSessionToken.newInstance()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyA9GWlmy5YiXZd2vQnQEjGn8eHts8XSxX0")
        }
        placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchBar = findViewById(R.id.searchbar)
        adapter = object : ArrayAdapter<String>(
            this,
            R.layout.itemseggustion,
            R.id.tvSuggestion,
            mutableListOf()
        ) {
            override fun getView(
                position: Int,
                convertView: android.view.View?,
                parent: android.view.ViewGroup
            ): android.view.View {
                val view = super.getView(position, convertView, parent)
                val tv = view.findViewById<TextView>(R.id.tvSuggestion)
                val icon = view.findViewById<ImageView>(R.id.iconSuggestion)
                tv.text = getItem(position)
                icon.setImageResource(R.drawable.pin)
                return view
            }
        }
        searchBar.setAdapter(adapter)
        searchBar.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.dropdownback
            )
        )
        searchBar.dropDownWidth = resources.displayMetrics.widthPixels - 100
        searchBar.dropDownHeight = 800
        WindowCompat.setDecorFitsSystemWindows(window, false)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.length >= 3) {
                    fetchAutocompletePredictions(query)
                } else {
                    adapter.clear()
                }
            }
        })

        searchBar.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = predictionList.getOrNull(position)
            selectedPrediction?.let {
                fetchPlaceAndShow(it)
                searchBar.dismissDropDown()
                searchBar.clearFocus()
                hideKeyboard()

            }
        }
        searchBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN)
            ) {

                searchBar.dismissDropDown() // hide dropdown
                searchBar.clearFocus()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocationAndStore()

        binding.btnNavigate.setOnClickListener {
            selectedDestination?.let { destination ->
                val origin = currentLatLng

                if (origin != null) {
                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        origin.latitude, origin.longitude,
                        destination.latitude, destination.longitude,
                        distance
                    )

                    if (distance[0] <= ARRIVAL_DISTANCE_METERS) {
                        // Always send notification if close
                        showArrivalNotification() // optional if you want to notify immediately
                    } else {
                        // User is far → hide slide button
                        binding.slidebotton.visibility = View.INVISIBLE
                    }}

                val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No destination selected", Toast.LENGTH_SHORT).show()
            }
        }

        handleDestinationFromIntent(intent)

        // Check if launched from notification
        val fromNotification = intent.getBooleanExtra("from_notification", false)
        if (fromNotification) {
            binding.slidebotton.visibility = View.VISIBLE
        } else {
            binding.slidebotton.visibility = View.INVISIBLE
        }

        binding.slidebotton.setOnClickListener {
                val i = Intent(this@MapsActivity, infodelivreur2::class.java)
                    i.putExtra("place_name",  selectedPlaceName)
                startActivity(i)
            }

    }


    private fun fetchAutocompletePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
           // .setCountries("DZ")
            .setTypesFilter(listOf("establishment")) // shops, restaurants, etc.
            .setSessionToken(sessionToken)// Restrict results to Algeria 🇩🇿
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictionList.clear()
                predictionList.addAll(response.autocompletePredictions)
                val suggestions = predictionList.map { it.getFullText(null).toString() }
                adapter.clear()
                adapter.addAll(suggestions)
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this, "can't search without internet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPlaceAndShow(prediction: AutocompletePrediction) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng


                place.latLng?.let {
                    selectedDestination = it
                    selectedPlaceName = place.name
                    if (latLng != null) {
                        selectedDestination = latLng
                        startProximityCheck()

                    currentLatLng?.let { origin ->
                        val distance = FloatArray(1)
                        Location.distanceBetween(
                            origin.latitude, origin.longitude,
                            latLng.latitude, latLng.longitude,
                            distance
                        )

                        if (distance[0] <= ARRIVAL_DISTANCE_METERS) {
                            showArrivalNotification()
                            binding.slidebotton.visibility = View.VISIBLE
                            binding.btnNavigate.visibility = View.INVISIBLE
                        } else {
                            binding.slidebotton.visibility = View.INVISIBLE
                            binding.btnNavigate.visibility = View.VISIBLE
                        }
                    }}


                    searchMarker?.remove()
                    searchMarker = map.addMarker(MarkerOptions().position(it).title(place.name))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f), 2000, null)
                    searchBar.setText(place.name)
                    binding.btnNavigate.visibility  = View.VISIBLE
                    currentLatLng?.let { origin ->
                        drawRoute(origin, it)
                    } ?: Toast.makeText(this, "Current location not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch place details", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        checkLocationPermissionAndEnable()
    }

    private fun checkLocationPermissionAndEnable() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                enableMyLocation()
            } else {
                showGPSDialog()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGPSDialog() {
        val alertDialog = Dialog(this)
        alertDialog.setContentView(R.layout.alertdialog)

        alertDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        alertDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val activer = alertDialog.findViewById<LinearLayout>(R.id.active)
        val desactiver = alertDialog.findViewById<LinearLayout>(R.id.desactive)

        activer.setOnClickListener {
            startActivityForResult(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                GPS_REQUEST_CODE
            )
            alertDialog.dismiss()
        }
        desactiver.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()

    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, // Accuracy level
                2000 // Interval in milliseconds
            ).setMinUpdateIntervalMillis(2000).build()

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)

                    val cameraPosition = CameraPosition.Builder()
                        .target(currentLatLng!!)
                        .zoom(17f)      // closer zoom for 3D effect
                        .tilt(50f)      // tilt the camera (0 = flat, 90 = straight down)
                        .bearing(0f)    // rotate (0 = north, 90 = east)
                        .build()

                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(cameraPosition),
                        2000,
                        null
                    )

                    // ✅ If we already have a destination, redraw the route
                    selectedDestination?.let { dest ->
                        drawRoute(currentLatLng!!, dest)
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkLocationPermissionAndEnable()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_REQUEST_CODE) {
            if (isLocationEnabled()) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "GPS is still disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(searchBar.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (isLocationEnabled &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1) // Only get one update
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    currentLatLng = LatLng(location.latitude, location.longitude)

                    val cameraPosition = CameraPosition.Builder()
                        .target(currentLatLng!!)
                        .zoom(17f)
                        .tilt(45f)
                        .bearing(0f)
                        .build()

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                    // ✅ If we already have a destination and map is ready, draw the route
                    if (::map.isInitialized && selectedDestination != null) {
                        drawRoute(currentLatLng!!, selectedDestination!!)
                        showDestinationMarker(selectedDestination!!)
                    }

                    // Stop further updates
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }, Looper.getMainLooper())
        }
    }


    private fun getCurrentLocationAndStore() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyA9GWlmy5YiXZd2vQnQEjGn8eHts8XSxX0" // Same key as your Maps API
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving" +
                "&key=$apiKey"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val data = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val jsonObject = JSONObject(data)
                val routes = jsonObject.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    val decodedPath = PolyUtil.decode(points)

                    runOnUiThread {
                        currentPolyline?.remove() // Remove old route
                        currentPolyline = map.addPolyline(
                            PolylineOptions()
                                .addAll(decodedPath)
                                .width(10f)
                                .color(Color.BLUE)
                                .geodesic(true)
                        )
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun startProximityCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000 // check every 3 seconds
        ).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return

                val dest = selectedDestination ?: return
                val distance = FloatArray(1)
                Location.distanceBetween(
                    location.latitude, location.longitude,
                    dest.latitude, dest.longitude,
                    distance
                )

                if (distance[0] <= ARRIVAL_DISTANCE_METERS && !hasSentArrivalNotification) {
                    hasSentArrivalNotification = true
                    showArrivalNotification()
                }
            }
        }, Looper.getMainLooper())
    }
    private fun showArrivalNotification() {
        val channelId = "arrival_channel_v2" // ✅ new ID so vibration works

        // Create notification channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Arrival Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when you are near your destination"
                enableVibration(true) // ✅ enable vibration
                vibrationPattern = longArrayOf(0, 5000, 500, 5000) // custom pattern
                enableLights(true)
                lightColor = Color.RED
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Intent to open arrival confirmation screen
        val intent = Intent(this, MapsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            selectedDestination?.let {
                putExtra("dest_lat", it.latitude)
                putExtra("dest_lng", it.longitude)
            }
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.mapmarkerblue)
            .setContentTitle("Avez vous arrivé?")
            .setContentText("Cliquez sur cette notification lorsque vous arrivez.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // ✅ sound + vibrate + lights
            .setVibrate(longArrayOf(0, 5000, 500, 5000,)) // ✅ force vibration at builder level too
            .build()

        // Android 13+ needs POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
                return
            }
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, notification)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // update stored intent
        handleDestinationFromIntent(intent)

        val fromNotification = intent.getBooleanExtra("from_notification", false)
        if (fromNotification) {
            binding.slidebotton.visibility = View.VISIBLE
            binding.btnNavigate.visibility = View.INVISIBLE
        } else {
            binding.slidebotton.visibility = View.GONE
            binding.slidebotton.visibility = View.INVISIBLE

        }

    }

    private fun handleDestinationFromIntent(intent: Intent) {
        val lat = intent.getDoubleExtra("dest_lat", Double.NaN)
        val lng = intent.getDoubleExtra("dest_lng", Double.NaN)
        if (!lat.isNaN() && !lng.isNaN()) {
            selectedDestination = LatLng(lat, lng)

            // If we already know current location, draw route immediately
            currentLatLng?.let { origin ->
                drawRoute(origin, selectedDestination!!)
                showDestinationMarker(selectedDestination!!)
                searchMarker?.remove()
                searchMarker = map.addMarker(
                    MarkerOptions().position(selectedDestination!!).title("Destination")
                )

                // Move camera to fit both points
                val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                    .include(origin)
                    .include(selectedDestination!!)
                    .build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }
    }
    private fun showDestinationMarker(destination: LatLng) {
        searchMarker?.remove() // remove old one if exists
        searchMarker = map.addMarker(
            MarkerOptions()
                .position(destination)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }
    private fun isCloseToDestination(): Boolean {
        val origin = currentLatLng ?: return false
        val dest = selectedDestination ?: return false

        val distance = FloatArray(1)
        Location.distanceBetween(
            origin.latitude, origin.longitude,
            dest.latitude, dest.longitude,
            distance
        )

        return distance[0] <= ARRIVAL_DISTANCE_METERS
    }




}









