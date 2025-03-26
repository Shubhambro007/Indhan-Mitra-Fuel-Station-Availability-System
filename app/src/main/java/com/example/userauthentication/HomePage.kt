package com.example.userauthentication

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.location.LocationManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

data class FuelStation(
    val id: String,
    val name: String,
    val location: LatLng,
    val type: String,
    val fuelTypes: List<String> = emptyList(),
    val rating: Float = 0.0f,
    val vicinity: String = "",
    var distance: Double = 0.0,
    var arrivalTime: String = "",
    val isOpen: Boolean = true,
    val pricePerUnit: Double = 0.0,
    var trafficDuration: String? = null,
    var trafficStatus: String? = null,
    var isTrafficDataReliable: Boolean = true // New property for traffic data reliability
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fuelStations by remember { mutableStateOf<List<FuelStation>>(emptyList()) }
    var selectedStation by remember { mutableStateOf<FuelStation?>(null) }
    var showStationDetails by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()

    var isLoading by remember { mutableStateOf(true) }
    var isBottomPanelExpanded by remember { mutableStateOf(true) }
    var bottomPanelHeight by remember { mutableStateOf(400.dp) }
    var showAlertDialog by remember { mutableStateOf(false) }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isTrafficEnabled = true // Enable traffic layer
            )
        )
    }

    val scope = rememberCoroutineScope()

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isMobileDataEnabled = isMobileDataEnabled(connectivityManager)

    if (!isLocationEnabled || !isMobileDataEnabled) {
        showAlertDialog = true
    }

    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Enable Location and Mobile Data") },
            text = {
                Text("Please enable location services and mobile data to use this app.")
            },
            confirmButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("OK")
                }
            }
        )
    } else {
        LaunchedEffect(locationPermissionState.allPermissionsGranted) {
            if (locationPermissionState.allPermissionsGranted) {
                isLoading = true
                getCurrentLocation(fusedLocationProviderClient, context) { location, error ->
                    currentLocation = location
                    errorMessage = error
                    if (location != null) {
                        // Start a coroutine to fetch nearby fuel stations
                        scope.launch {
                            fetchNearbyFuelStations(location.latitude, location.longitude) { stations, err ->
                                fuelStations = stations
                                errorMessage = err
                                isLoading = false

                                // Start polling for traffic data in a separate coroutine
                                scope.launch {
                                    while (true) {
                                        stations.forEach { station ->
                                            fetchTrafficData(location, station.location) { trafficDuration, trafficStatus, isReliable ->
                                                fuelStations = fuelStations.map {
                                                    if (it.id == station.id) {
                                                        it.copy(trafficDuration = trafficDuration, trafficStatus = trafficStatus, isTrafficDataReliable = isReliable)
                                                    } else {
                                                        it
                                                    }
                                                }
                                            }
                                        }
                                        delay(60000) // Poll every minute
                                    }
                                }
                            }
                        }
                    } else {
                        errorMessage = "Unable to get current location. Please ensure location services are enabled."
                        isLoading = false
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (locationPermissionState.allPermissionsGranted && currentLocation != null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 13f)

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(compassEnabled = false, zoomControlsEnabled = false)
                ) {
                    if (currentLocation != null) {
                        Marker(
                            state = MarkerState(position = currentLocation!!),
                            title = "Your Location",
                            snippet = "Current position",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }

                    fuelStations.forEach { station ->
                        val markerColor = when (station.type) {
                            "CNG Station" -> BitmapDescriptorFactory.HUE_GREEN
                            "EV Charging Station" -> BitmapDescriptorFactory.HUE_BLUE
                            "Diesel Station" -> BitmapDescriptorFactory.HUE_YELLOW
                            else -> BitmapDescriptorFactory.HUE_RED
                        }

                        Marker(
                            state = MarkerState(position = station.location),
                            title = station.name,
                            snippet = station.type,
                            icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                            onClick = {
                                selectedStation = station
                                showStationDetails = true
                                isBottomPanelExpanded = false
                                false
                            }
                        )
                    }

                    currentLocation?.let {
                        Circle(
                            center = it,
                            radius = 5000.0,
                            strokeColor = Color(0x664285F4),
                            fillColor = Color(0x334285F4),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Indhan",
                                color = Color(0xFF000000),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            Text(
                                text = " मित्र",
                                color = Color(0xFF000000),
                                fontSize = 20.sp
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF212121))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    isBottomPanelExpanded = !isBottomPanelExpanded
                                    bottomPanelHeight =
                                        if (isBottomPanelExpanded) 400.dp else 100.dp
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        color = Color.Gray,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Choose a fuel station",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "${fuelStations.size} stations found",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isBottomPanelExpanded) 200.dp else 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.Green)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = bottomPanelHeight)
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(fuelStations) { station ->
                                    FuelStationItem(
                                        station = station,
                                        onClick = {
                                            scope.launch {
                                                cameraPositionState.animate(
                                                    update = CameraUpdateFactory.newLatLngZoom(
                                                        station.location,
                                                        15f
                                                    ),
                                                    durationMs = 1000
                                                )
                                                selectedStation = station
                                                showStationDetails = true
                                            }
                                        }
                                    )
                                    Divider(
                                        color = Color(0xFF333333),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showStationDetails,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                selectedStation?.let { station ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = Color.Black,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF333333))
                                        .clickable {
                                            showStationDetails = false
                                            isBottomPanelExpanded = true
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = station.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )

                                Text(
                                    text = if (station.isOpen) "OPEN" else "CLOSED",
                                    color = if (station.isOpen) Color.Green else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = station.vicinity,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Traffic Status: ${station.trafficStatus ?: "Loading..."}",
                                color = Color.Cyan,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Fuel Type",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = station.type,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Price",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "₹${station.pricePerUnit}/unit",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Distance",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${String.format("%.1f", station.distance)} km",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val uri = Uri.parse("google.navigation:q=${station.location.latitude},${station.location.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                    mapIntent.setPackage("com.google.android.apps.maps")

                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: ActivityNotFoundException) {
                                        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${station.location.latitude},${station.location.longitude}")
                                        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                                        context.startActivity(browserIntent)
                                    }

                                    showStationDetails = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0066CC)
                                )
                            ) {
                                Text("Get Directions")
                            }
                        }
                    }
                }
            }

            errorMessage?.let {
                AlertDialog(
                    onDismissRequest = { errorMessage = null },
                    title = { Text("Error") },
                    text = { Text(it) },
                    confirmButton = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (!locationPermissionState.allPermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Location permission is required to find nearby fuel stations.",
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

private fun isMobileDataEnabled(connectivityManager: ConnectivityManager): Boolean {
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    context: Context,
    callback: (LatLng?, String?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback(LatLng(location.latitude, location.longitude), null)
                } else {
                    callback(null, "Could not determine your location.")
                }
            }
            .addOnFailureListener { exception ->
                callback(null, "Could not determine your location.")
            }
    } else {
        callback(null, "Location permission not granted.")
    }
}

private suspend fun fetchNearbyFuelStations(
    latitude: Double,
    longitude: Double,
    callback: (List<FuelStation>, String?) -> Unit
) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val stations = mutableListOf<FuelStation>()
        var error: String? = null

        try {
            val apiKey = "AIzaSyAEVtzHmbHQkK5YZk6NamZlLHoExAR6fRg"

            // Fetch all gas stations
            val gasStationUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=$latitude,$longitude" +
                    "&radius=5000" +
                    "&type=gas_station" + // Fetch all types of gas stations
                    "&key=$apiKey"

            val gasStationRequest = Request.Builder()
                .url(gasStationUrl)
                .build()

            client.newCall(gasStationRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData!!)
                val results = jsonObject.getJSONArray("results")

                for (i in 0 until results.length()) {
                    val placeJson = results.getJSONObject(i)

                    val id = placeJson.getString("place_id")
                    val name = placeJson.getString("name")

                    val locationJson = placeJson.getJSONObject("geometry").getJSONObject("location")
                    val lat = locationJson.getDouble("lat")
                    val lng = locationJson.getDouble("lng")
                    val placeLocation = LatLng(lat, lng)

                    val vicinity = if (placeJson.has("vicinity")) placeJson.getString("vicinity") else ""
                    val isOpen = if (placeJson.has("opening_hours")) {
                        placeJson.getJSONObject("opening_hours").optBoolean("open_now", true)
                    } else {
                        true
                    }

                    val finalStationType = determineFuelStationType(name)
                    val fuelTypes = getFuelTypesForStation(finalStationType)
                    val price = getStandardPriceForFuelType(finalStationType)
                    val rating = if (placeJson.has("rating")) placeJson.getDouble("rating").toFloat() else 0.0f

                    // Fetch distance and arrival time from Directions API
                    val (distance, arrivalTime) = fetchDistanceAndArrivalTime(LatLng(latitude, longitude), placeLocation, apiKey)

                    // Assume traffic data is reliable by default
                    val isTrafficDataReliable = true // You can implement logic to determine this based on your needs

                    stations.add(
                        FuelStation(
                            id = id,
                            name = name,
                            location = placeLocation,
                            type = finalStationType,
                            fuelTypes = fuelTypes,
                            rating = rating,
                            vicinity = vicinity,
                            distance = distance,
                            arrivalTime = arrivalTime,
                            isOpen = isOpen,
                            pricePerUnit = price,
                            isTrafficDataReliable = isTrafficDataReliable // Set reliability
                        )
                    )
                }
            }

            // Sort stations by distance
            val sortedStations = stations.sortedBy { it.distance }
            callback(sortedStations.distinctBy { it.id }, null)

        } catch (e: Exception) {
            callback(emptyList(), "Failed to fetch fuel stations: ${e.message}")
        }
    }
}

private suspend fun fetchDistanceAndArrivalTime(
    origin: LatLng,
    destination: LatLng,
    apiKey: String
): Pair<Double, String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&key=$apiKey"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData!!)
                val routes = jsonObject.getJSONArray("routes")

                if (routes.length() > 0) {
                    val leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    val distanceValue = leg.getJSONObject("distance").getDouble("value") // Distance in meters
                    val distanceKm = distanceValue / 1000 // Convert to kilometers
                    val arrivalTime = leg.getJSONObject("duration").getString("text") // Duration in traffic

                    return@withContext Pair(distanceKm, arrivalTime)
                } else {
                    return@withContext Pair(0.0, "Unknown")
                }
            }
        } catch (e: Exception) {
            Pair(0.0, "Failed to fetch distance and arrival time: ${e.message}")
        }
    }
}

private fun determineFuelStationType(name: String): String {
    return when {
        name.contains("CNG", ignoreCase = true) -> "CNG Station"
        name.contains("Electric", ignoreCase = true) ||
                name.contains("EV", ignoreCase = true) ||
                name.contains("Charging", ignoreCase = true) -> "EV Charging Station"
        name.contains("Diesel", ignoreCase = true) -> "Diesel Station"
        else -> "Petrol Station"
    }
}

private fun getFuelTypesForStation(stationType: String): List<String> {
    return when (stationType) {
        "CNG Station" -> listOf("CNG", "Petrol")
        "EV Charging Station" -> listOf("Electric")
        "Diesel Station" -> listOf("Diesel")
        else -> listOf("Petrol", "Diesel")
    }
}

private fun getStandardPriceForFuelType(stationType: String): Double {
    return when (stationType) {
        "CNG Station" -> 85.50
        "EV Charging Station" -> 12.00
        "Diesel Station" -> 89.75
        else -> 105.50 // Petrol
    }
}

private suspend fun fetchTrafficData(
    origin: LatLng,
    destination: LatLng,
    callback: (String?, String?, Boolean) -> Unit // Updated to include reliability
) {
    withContext(Dispatchers.IO) {
        try {
            val apiKey = "AIzaSyAEVtzHmbHQkK5YZk6NamZlLHoExAR6fRg"
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&departure_time=now" +
                    "&key=$apiKey"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData!!)
                val routes = jsonObject.getJSONArray("routes")

                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val legs = route.getJSONArray("legs")
                    if (legs.length() > 0) {
                        val leg = legs.getJSONObject(0)
                        val duration = leg.getJSONObject("duration_in_traffic").getString("text")
                        val durationValue = leg.getJSONObject("duration_in_traffic").getInt("value") // Duration in seconds

                        val trafficStatus = when {
                            durationValue < 600 -> "Low Traffic"
                            durationValue < 1800 -> "Medium Traffic"
                            else -> "High Traffic"
                        }

                        // Set reliability based on the response
                        val isTrafficDataReliable = true // You can implement logic to determine this based on your needs
                        callback(duration, trafficStatus, isTrafficDataReliable) // Return both duration, status, and reliability
                    } else {
                        callback(null, null, false)
                    }
                } else {
                    callback(null, null, false)
                }
            }
        } catch (e: Exception) {
            callback("Failed to fetch traffic data: ${e.message}", null, false)
        }
    }
}

@Composable
fun FuelStationItem(station: FuelStation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF212121)),
            contentAlignment = Alignment.Center
        ) {
            val iconColor = when (station.type) {
                "CNG Station" -> Color(0xFF4CAF50)
                "EV Charging Station" -> Color(0xFF2196F3)
                "Diesel Station" -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }

            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = station.type,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = station.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.arrivalTime,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = " · ${String.format("%.1f", station.distance)} km away",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                station.fuelTypes.take(3).forEach { fuelType ->
                    val chipColor = when (fuelType) {
                        "CNG" -> Color(0xFF4CAF50)
                        "Electric" -> Color(0xFF2196F3)
                        "Diesel" -> Color(0xFFFFC107)
                        "Petrol" -> Color(0xFFF44336)
                        else -> Color(0xFF9E9E9E)
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(chipColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = fuelType,
                            color = chipColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(8.dp)
                        .background(if (station.isOpen) Color.Green else Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (station.isOpen) "Open" else "Closed",
                    color = if (station.isOpen) Color.Green else Color.Red,
                    fontSize = 10.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = station.trafficStatus ?: "Loading...",
                color = Color.Cyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = "₹${station.pricePerUnit}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (station.pricePerUnit < 90.0) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF4CAF50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Best Price",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Best Price",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 700)
@Composable
fun HomePagePreview() {
    HomePage(rememberNavController())
}