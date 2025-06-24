package com.example.bucovinawanders.ui.screens

import android.Manifest
import android.annotation.*
import android.content.pm.*
import androidx.compose.animation.*
import androidx.core.content.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.*

import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import coil.compose.*
import kotlinx.coroutines.*

import com.example.bucovinawanders.R
import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.ui.screens.obiective.*
import com.example.bucovinawanders.ui.theme.*
import com.example.bucovinawanders.utils.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(navController: NavController, token: String? = null, obiectivId: Int = -1, routePoints: List<LatLng> = emptyList(), onRoutePointsChange: (List<LatLng>) -> Unit = {}) {
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
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    var showExploreSheet by remember { mutableStateOf(false) } //afiseaza sheet-ul de explorare

    var searchQuery by remember { mutableStateOf("") } //textul introdus in bara de cautare
    val focusManager = LocalFocusManager.current //pentru a inchide bara de cautare
    var isSuggestionVisible by remember { mutableStateOf(false) }

    //lista cu tipurile de obiective pentru filtare
    val tipuri = listOf(
        "Cetate" to 1,
        "Statuie" to 2,
        "Muzeu" to 3,
        "Parc" to 4,
        "Manastire" to 5
    )

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

    //temperatura
    LaunchedEffect(userLocation?.latitude, userLocation?.longitude) {
        userLocation?.let { loc ->
            val tempC = getTemperature(loc.latitude, loc.longitude) //obtine temperatura in celsiusius
            temperatureCelsius = tempC
            temperatureFahrenheit = tempC.replace("°C", "").toDoubleOrNull()?.let { c ->
                "${(c * 9 / 5 + 32).toInt()}°F" //convertim temperatura in fahrenheit
            }
        }
    }

    //serach bar
    LaunchedEffect(searchQuery, obiective) {
        filteredObiective = if (searchQuery.isBlank()) { //daca nu am scris nimic in bara de cautare se afiseaza toate obiectivele
            obiective
        } else {
            obiective.filter { //altfel se afiseaza doar obiectivele care contin textul introdus in bara de cautare
                it.nume.contains(searchQuery.trim(), ignoreCase = true)
            }
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

    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            routePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
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


    //UI principal
    Box(Modifier.fillMaxSize()) {
        //harta cu obiectivele
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

        //seachbar
        OutlinedTextField(
            value = searchQuery, //textul introdus in bara de cautare
            onValueChange = {
                searchQuery = it
                isSuggestionVisible = it.isNotBlank() // arătăm sugestiile doar dacă există text
            }, //actualizeaza textul introdus in bara de cautare cand userul tasteaza
            placeholder = { Text("Cauta..", color = unselectedTextColor) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = pressedColor, //border cand este activ
                unfocusedBorderColor = unselectedTextColor, //border cand nu este activ
                cursorColor = pressedColor,
                focusedLabelColor = pressedColor,
                unfocusedLabelColor = unselectedTextColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor
            ),
            textStyle = LocalTextStyle.current.copy(color = textColor), //stilul textului
            singleLine = true, //permite o singura linie de text
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search), //la tastare apara butonul de cautare pe tastatura
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus() //ascundem tastatura cand userul apasa butonul de cautare

                    val obiectivGasit = filteredObiective.firstOrNull { //cauta primul obiectiv care contine textul introdus in bara de cautare
                        it.nume.contains(searchQuery.trim(), ignoreCase = true)
                    }

                    if (obiectivGasit != null) {
                        coroutineScope.launch { //muta camera la obiectivul gasit
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(obiectivGasit.coordonataX, obiectivGasit.coordonataY),
                                    18f
                                )
                            )
                            selectedObiectiv = obiectivGasit //setam obiectivul ca selectat pentru a afisa detaliile
                        }
                    }

                    searchQuery = "" // resetăm bara de căutare DUPĂ search
                    isSuggestionVisible = false // ascundem sugestiile
                }
            )
        )

        //butoane pentru filtrare
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 128.dp)
                .horizontalScroll(rememberScrollState()), //scroll horizontal
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tipuri.forEach { (label, idTip) -> //pentru fiecare tip de obiectiv se creeaza un buton
                Button(
                    onClick = {
                        selectedTip = if (selectedTip == idTip) -1 else idTip //daca butonul este selectat se dezactiveaza, altfel se activeaza
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors( //culori diferite daca butonul este selectat
                        containerColor = if (selectedTip == idTip) pressedColor else backgroundColor,
                        contentColor = if (selectedTip == idTip) textColor else unselectedTextColor
                    )
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
                .padding(start = 16.dp, top = 256.dp),
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
                .padding(start = 16.dp, top = 192.dp),
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
                .padding(end = 16.dp, top = 192.dp),
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

        //sugestii
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 128.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = isSuggestionVisible, //vizibil doar cand isSuggestionVisible == true
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top), //animatie de intrare
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top) //animatie de iesire
            ) {
                //coloana cu sugestiile
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(
                            backgroundColor.copy(alpha = 0.95f), //fundal semi-transparent
                            shape = RoundedCornerShape(20.dp)
                        )
                        .fillMaxWidth(0.8f)
                        .border(1.dp, pressedColor, RoundedCornerShape(20.dp))
                ) {
                    filteredObiective.take(5).forEach { obiectiv -> //afiseaza primele 5 obiective din lista de sugestii
                        Text( //fiecare sugestie este un text clickabil
                            text = obiectiv.nume,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch { //muta camera la obiectivul selectat
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(obiectiv.coordonataX, obiectiv.coordonataY),
                                                18f
                                            )
                                        )
                                        selectedObiectiv = obiectiv
                                    }

                                    searchQuery = "" //resetam textul din searchbar
                                    isSuggestionVisible = false //ascundem sugestiile
                                    focusManager.clearFocus() //scoatem focusul de la searchbar
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),

                            color = textColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        //afiseaza ExploreScreen daca showExploreSheet e true
        if (showExploreSheet) {
            ExploreScreen(
                obiective = obiective,
                onDismiss = { showExploreSheet = false },
                onObiectivClick = { obiectiv ->
                    showExploreSheet = false //inchide sheet-ul
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(obiectiv.coordonataX, obiectiv.coordonataY),
                                18f //muta camera la obiectivul selectat
                            )
                        )
                        selectedObiectiv = obiectiv //arata detaliile pentru obiectivul selectat
                    }
                }
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
            onClick = { showExploreSheet = true }, //afisam sheet-ul de explorare
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
                Icons.Default.Explore,
                contentDescription = "Explore",
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
            onClick = { navController.navigate("savesScreen") }, //navigare catre pagina de favorite
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
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Favorites",
                modifier = Modifier.size(32.dp)
            )
        }

    }
}