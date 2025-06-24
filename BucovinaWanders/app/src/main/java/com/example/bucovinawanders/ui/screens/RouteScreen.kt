@file:Suppress("DEPRECATION")

package com.example.bucovinawanders.ui.screens

import android.Manifest
import android.annotation.*
import android.content.pm.*
import android.location.Location
import android.os.Looper
import androidx.core.content.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.navigation.*


import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import coil.compose.*

import com.example.bucovinawanders.R
import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.ui.screens.obiective.*
import com.example.bucovinawanders.ui.theme.*
import com.example.bucovinawanders.utils.*
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RouteScreen(navController: NavController, token: String? = null, obiectivId: Int = -1, routePoints: List<LatLng>, onRoutePointsChange: (List<LatLng>) -> Unit = {}, onBack: () -> Unit) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) //pentru permisiunea de a accesa localizarea

    var userLocation by remember { mutableStateOf<LatLng?>(null) } //stare pentru locatia utilizatorului
    val cameraPositionState = rememberCameraPositionState() //stare pentru pozitia camerei pe harta
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) } //client pentru a obtine ultima locatie
    val coroutineScope = rememberCoroutineScope() //pentru a executa corutine

    var obiective by remember { mutableStateOf(emptyList<ObiectivTuristicModel>()) } //lista de obiective din baza de date
    var filteredObiective by remember { mutableStateOf(emptyList<ObiectivTuristicModel>()) } //lista de obiective afisate in functie de filtru
    var selectedTip by remember { mutableIntStateOf(-1) } //tipul de obiectiv selectat
    var selectedObiectiv by remember { mutableStateOf<ObiectivTuristicModel?>(null) } //obiectivul selectat

    var compassRotation by remember { mutableFloatStateOf(0f) } //rotatia busolnei

    var temperatureCelsius by remember { mutableStateOf<String?>(null) } //temperatura in celsiusius
    var temperatureFahrenheit by remember { mutableStateOf<String?>(null) } //temperatura in fahrenheit
    var showCelsius by remember { mutableStateOf(true) } //afiseaza temperatura in celsiusius sau in fahrenheit

    //detecteaza daca tema este intunecata
    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())

    //culori pentru butoane si text in functie de tema
    val pressedColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    var gpsFollowMode by remember { mutableStateOf(false) }
    var lastUserLocation by remember { mutableStateOf<LatLng?>(null) }

    var showInstructions by remember { mutableStateOf(false) }


    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            routePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    //filtrare obiective
    LaunchedEffect(selectedTip) {
        coroutineScope.launch {
            try {
                if (selectedTip == -1) {
                    //afiseaza toate obiectivele in cazul in care nu este selectat niciun tip
                    val response = ApiClient.obiectiveApi.getObiective()
                    obiective = response
                    filteredObiective = response
                } else {
                    //afiseaza doar obiectivele cu tipul selectat
                    val response = ApiClient.obiectiveApi.getObiectiveByTip(selectedTip)
                    obiective = response
                    filteredObiective = response
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //user location
    LaunchedEffect(locationPermissionState.status) {
        //daca permisiunea este acordata se obtine ultima locatie si se afiseaza pe harta
        if (locationPermissionState.status.isGranted) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude) //salveaza ultima locatie in userLocation
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLocation!!, 18f)) //muta camera pe harta la ultima locatie
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest() //in caz contrar se cere permisiunea de a accesa localizarea
        }
    }

    //compass
    LaunchedEffect(cameraPositionState.position) {
        compassRotation = cameraPositionState.position.bearing //actualizeaza rotatia busolnei la pozitia camerei pe harta
    }

    LaunchedEffect(userLocation?.latitude, userLocation?.longitude) {
        userLocation?.let { loc ->
            val tempC = getTemperature(loc.latitude, loc.longitude)
            temperatureCelsius = tempC
            temperatureFahrenheit = tempC.replace("°C", "").toDoubleOrNull()?.let { c ->
                "${(c * 9 / 5 + 32).toInt()}°F"
            }
        }
    }

    //facus pe un anumit obiectiv
    LaunchedEffect(obiectivId, obiective) {
        if (obiectivId != -1) {
            val obiectiv = obiective.find { it.idObiectiv == obiectivId } //gaseste obiectivul cu id-ul respectiv
            obiectiv?.let { //daca exista se afiseaza pe harta
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.coordonataX, it.coordonataY), 18f) //muta camera la obiectivul selectat
                )
                selectedObiectiv = it
            }
        }
    }

    var maxDistance by remember { mutableStateOf(10f) }

    LaunchedEffect(Unit) {
        DistancePreferences.getDistance(context).collect {
            maxDistance = it
        }
    }

    LaunchedEffect(userLocation, obiective, maxDistance) {
        if (userLocation != null) {
            filteredObiective = obiective.filter { obiectiv ->
                val obiectivLocation = LatLng(obiectiv.coordonataX, obiectiv.coordonataY)
                val dist = distanceInKm(userLocation!!, obiectivLocation)
                dist <= maxDistance
            }
        }
    }

    LaunchedEffect(routePoints, userLocation, gpsFollowMode) {
        if (!gpsFollowMode && routePoints.isNotEmpty() && userLocation != null) {
            val boundsBuilder = LatLngBounds.builder()
            boundsBuilder.include(userLocation!!)
            routePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 150)
            )
        }
    }

    val locationCallback = rememberUpdatedState(object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            val newLatLng = LatLng(location.latitude, location.longitude)

            val fromLocation = Location("").apply {
                latitude = lastUserLocation?.latitude ?: 0.0
                longitude = lastUserLocation?.longitude ?: 0.0
            }
            val toLocation = Location("").apply {
                latitude = newLatLng.latitude
                longitude = newLatLng.longitude
            }

            val bearing = fromLocation.bearingTo(toLocation)

            lastUserLocation = newLatLng
            userLocation = newLatLng

            // urmareste automat camera pe utilizator
            if (gpsFollowMode) {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(newLatLng)
                                .zoom(18f)
                                .bearing(bearing)
                                .tilt(45f)
                                .build()
                        )
                    )
                }
            }

            //actualizare progres pe ruta
            if (routePoints.isNotEmpty()) {
                val newRoute = getRemainingRoute(newLatLng, routePoints)
                if (newRoute != routePoints) {
                    onRoutePointsChange(newRoute)
                }

                //recalculeaza ruta daca e in afara traseului
                if (isUserOffRoute(newLatLng, routePoints) && obiectivId != -1) {
                    coroutineScope.launch {
                        try {
                            val newRouteFromApi = ApiClient.routesApi.genereazaRuta(
                                userLat = newLatLng.latitude,
                                userLng = newLatLng.longitude,
                                obiectiveIds = listOf(obiectivId)
                            )

                            val updatedRoute = newRouteFromApi.geojson.features
                                .flatMap { it.geometry.coordinates }
                                .map { coord -> LatLng(coord[1], coord[0]) }

                            onRoutePointsChange(updatedRoute)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    })

    LaunchedEffect(gpsFollowMode) {
        if (gpsFollowMode) {
            fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 2000
                    fastestInterval = 1000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                locationCallback.value,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback.value)
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(), //dimensiunea hartii
            cameraPositionState = cameraPositionState, //stare pentru pozitia camerei
            properties = MapProperties( //proprietati ale hartii
                isMyLocationEnabled = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION //verifica daca permisiunea de a accesa localizarea este acordata
                ) == PackageManager.PERMISSION_GRANTED,
                mapStyleOptions = if (isDarkTheme) { //daca tema este intunecata se foloseste stilurile de dark
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
                } else {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_light)
                }
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = false
            )
        ) {
            //adaugam scala care depinde de zoomul actual
            val zoom = cameraPositionState.position.zoom
            val scale = (zoom / 20f).coerceIn(0.3f, 1f) //limita ca sa nu fie prea mic sau prea mare

            //adaugam un marker pentru ficare obiectiv
            filteredObiective.forEach { obiectiv ->
                Marker(
                    state = MarkerState(LatLng(obiectiv.coordonataX, obiectiv.coordonataY)),
                    title = obiectiv.nume,
                    icon = getCustomIcon(context, getTipById(obiectiv.idTip), isDarkTheme, scale),
                    onClick = {
                        selectedObiectiv = obiectiv
                        true
                    }
                )
            }

            if (routePoints.isNotEmpty() && userLocation != null) {
                val closestIndex = routePoints.indexOfFirst {
                    distanceInKm(userLocation!!, it) * 1000 < 20
                }.takeIf { it != -1 } ?: 0

                val traversed = routePoints.take(closestIndex + 1)
                val remaining = routePoints.drop(closestIndex)

                if (traversed.size > 1) {
                    Polyline(
                        points = traversed,
                        color = Color(0xFF4CAF50), // verde
                        width = 8f
                    )
                }

                if (remaining.size > 1) {
                    Polyline(
                        points = remaining,
                        color = MaterialTheme.colorScheme.primary,
                        width = 8f
                    )
                }
            }
        }

        //afiseaza panel cu detalii daca un obiectiv este selectat
        if (selectedObiectiv != null) {
            ObiectivDetailsScreen(
                obiectiv = selectedObiectiv,
                onDismiss = { selectedObiectiv = null },
                token = token,
                onRoutePointsChange = onRoutePointsChange,
                navController = navController
            )
        }

        //buton update location
        Button(
            onClick = {
                userLocation?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 18f)) //muta camera la noua locatie
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 128.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = pressedColor,
                contentColor = textColor
            )
        ) {
            Icon(
                painter = rememberAsyncImagePainter(R.drawable.icon_update_location),
                contentDescription = "My Location"
            )
        }

        //buton busola
        Button(
            onClick = {
                cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            cameraPositionState.position.target, //pastreaza pozitia camerei
                            cameraPositionState.position.zoom, //pastreaza zoomul camerei
                            0f, //reseteaza orientarea camerei la nord
                            cameraPositionState.position.tilt //pastreaza inclinarea hartii
                        )
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 64.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = pressedColor,
                contentColor = textColor
            )
        ) {
            Icon(
                painter = rememberAsyncImagePainter(R.drawable.icon_compass),
                modifier = Modifier.graphicsLayer { rotationZ = -compassRotation },
                contentDescription = "Reset Map Orientation"
            )
        }

        //temperatura
        Button(
            onClick = { showCelsius = !showCelsius }, //schimba afisarea temperaturii
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 64.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = pressedColor,
                contentColor = textColor
            )
        ) {
            Text(
                text = if (showCelsius) temperatureCelsius ?: "-" else temperatureFahrenheit
                    ?: "-"
            )
        }

        //design
        Canvas(
            modifier = Modifier
                .width(180.dp)
                .height(70.dp)
                .align(Alignment.BottomCenter)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                quadraticTo(size.width / 2, size.height * 1.5f, size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = path,
                color = pressedColor
            )
        }

        //design
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .align(Alignment.BottomCenter)
        ) {
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height)
                quadraticTo(size.width / 2, size.height * 1.6f, size.width * 0.8f, size.height)
                lineTo(size.width * 0.8f, 0f)
                lineTo(size.width * 0.2f, 0f)
                close()
            }
            drawPath(
                path = path,
                color = pressedColor
            )
        }

        //buton explore
        Button(
            onClick = {
                gpsFollowMode = !gpsFollowMode
                showInstructions = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = pressedColor,
                contentColor = textColor
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Start Navigare",
                modifier = Modifier.size(32.dp)
            )
        }

        //buton setari
        Button(
            onClick = { navController.navigate("settingsScreen") }, //navigare catre pagina de setari
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(150.dp)
                .height(76.2.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = pressedColor,
                contentColor = textColor
            )
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(32.dp)
            )
        }

        //buton salvari
        Button(
            onClick = onBack, //navigare catre pagina de favorite
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(150.dp)
                .height(76.2.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = pressedColor,
                contentColor = textColor
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}

fun getRemainingRoute(currentLocation: LatLng, fullRoute: List<LatLng>, thresholdMeters: Float = 20f): List<LatLng> {
    val currentIndex = fullRoute.indexOfFirst {
        distanceInKm(currentLocation, it) * 1000 < thresholdMeters
    }
    return if (currentIndex != -1) {
        fullRoute.drop(currentIndex)
    } else fullRoute
}

fun isUserOffRoute(userLocation: LatLng, routePoints: List<LatLng>, thresholdMeters: Float = 50f): Boolean {
    return routePoints.minOfOrNull { distanceInKm(userLocation, it) * 1000 }?.let { it > thresholdMeters } == true
}