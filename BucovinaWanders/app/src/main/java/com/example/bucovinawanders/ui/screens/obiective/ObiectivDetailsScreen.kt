package com.example.bucovinawanders.ui.screens.obiective

import android.Manifest
import android.os.Build
import android.content.pm.*
import android.widget.*

import androidx.annotation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*
import androidx.core.app.*
import androidx.navigation.*

import kotlinx.coroutines.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.models.users.*
import com.example.bucovinawanders.utils.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObiectivDetailsScreen(obiectiv: ObiectivTuristicModel?, token: String?, onDismiss: () -> Unit, onRoutePointsChange: (List<LatLng>) -> Unit, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var savedObjectives by remember { mutableStateOf<List<ObiectivTuristicModel>>(emptyList()) }
    var isSaved by remember { mutableStateOf(false) }
    var userHasReviewed by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(3) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewStatus by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var obiectivStatus by remember { mutableStateOf("Unknown") }

    //functie care afiseaza un pop-up cu un mesaj
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //resetam lista de obiective salvate cand se schimba token
    LaunchedEffect(token) {
        savedObjectives = emptyList()
        errorMessage = null
    }

    //verificam daca obiectivul este deja salvat
    LaunchedEffect(token, obiectiv?.idObiectiv) {
        if (!token.isNullOrEmpty() && obiectiv != null) {
            try {
                val response = ApiClient.savesApi.getSavedObiective("Bearer $token")
                if (response.isSuccessful) {
                    val savedList = response.body() ?: emptyList()
                    isSaved = savedList.any { it.idObiectiv == obiectiv.idObiectiv }
                }
            } catch (_: Exception) {
            }
        }
    }

    //verificam daca utilizatorul a dat deja un review
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = ApiClient.reviewsApi.hasReviewed("Bearer $token",
                    idObiectiv = obiectiv?.idObiectiv
                )
                if (response.isSuccessful) {
                    userHasReviewed = response.body() == true
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(obiectiv) {
        if (obiectiv != null) {
            try {
                val response = ApiClient.obiectiveApi.getObiectivStatus(obiectiv.idObiectiv)
                obiectivStatus = response.status
            } catch (_: Exception) {
                obiectivStatus = "Inchis"
            }
        }
    }

    //afisam detaliile obiectivului
    if (obiectiv != null) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                //header cu numele obiectivului
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = obiectiv.nume,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                //butoane
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //favorite
                    Button(
                        onClick = {
                            if (!token.isNullOrEmpty()) {
                                coroutineScope.launch {
                                    try {
                                        val response = if (!isSaved) {
                                            val request = UserSavesCreateModel(idObiectiv = obiectiv.idObiectiv)
                                            ApiClient.savesApi.saveObiectiv("Bearer $token", request)
                                        } else {
                                            ApiClient.savesApi.deleteObiectiv("Bearer $token", obiectiv.idObiectiv)
                                        }

                                        if (response.isSuccessful) {
                                            isSaved = !isSaved
                                            if (isSaved) {
                                                showToast("Obiectiv adaugat la favorite.")
                                            } else {
                                                showToast("Obiectiv sters de la favorite.")
                                            }
                                        } else {
                                            showToast("Eroare la salvarea obiectivului.")
                                        }
                                    } catch (_ : Exception) {
                                        showToast("Eroare la salvarea obiectivului.")
                                    }
                                }
                            } else {
                                showToast("Trebuie sa fii autentificat.")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = if (isSaved) "Sterge de la favorite." else "Adauga la favorite.",
                            tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    //vizitat
                    Button(
                        onClick = {
                            if (!token.isNullOrEmpty()) {
                                coroutineScope.launch {
                                    try {
                                        val request =
                                            UserVisitCreateModel(idObiectiv = obiectiv.idObiectiv)
                                        val response = ApiClient.visitsApi.visitObiectiv(
                                            "Bearer $token",
                                            request
                                        )
                                        if (response.isSuccessful) {
                                            showToast("Obiectiv marcat ca vizitat.")
                                        } else {
                                            showToast("Eroare la marcarea obiectivului ca vizitat.")
                                        }
                                    } catch (_ : Exception) {
                                        showToast("Eroare la marcarea obiectivului ca vizitat.")
                                    }
                                }
                            } else {
                                showToast("Trebuie sa fii autentificat.")
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.RemoveRedEye,
                            contentDescription = "Visited",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // directii - traseu pe hartă
                    Button(
                        onClick = {
                            if (token != null) {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    showToast("Permisiunea pentru locatie este necesara.")
                                    return@Button
                                }

                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            genereazaRutaPentruObiectiv(
                                                obiectiv = obiectiv,
                                                userLocation = location,
                                                showToast = { msg -> showToast(msg) },
                                                onRouteGenerated = { points ->
                                                    onRoutePointsChange(points)
                                                    navController.navigate("routeScreen")
                                                }
                                            )
                                        }
                                    } else {
                                        showToast("Nu s-a putut obtine locatia curenta.")
                                    }
                                }
                            } else {
                                showToast("Trebuie sa fii autentificat.")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Show route",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = obiectivStatus,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            color = when (obiectivStatus.lowercase()) {
                                "deschis" -> Color(0xFF4CAF50)
                                "inchis" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                //program de functionare
                val programList = programFormat(obiectiv.program)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Program",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Program:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        //listam programul
                        programList.forEach { (zi, interval, isToday) ->
                            Text(
                                text = "\t$zi: $interval",
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 32.dp, top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                //contact
                ContactInfoSection(obiectiv.contact)

                Spacer(modifier = Modifier.height(16.dp))

                //descriere
                if (!obiectiv.descriere.isNullOrEmpty()) {
                    Text(
                        text = "Descriere",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable description
                    val scrollState = rememberScrollState()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                    ) {
                        Box(modifier = Modifier.verticalScroll(scrollState)) {
                            Text(
                                text = obiectiv.descriere,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                //numar recenzii si nota recenzii
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                            Text(
                                text = "Rating: ${obiectiv.notaRecenzii?.twoDecimalPlaces() ?: "0.00"}/5",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "${obiectiv.numarRecenzii ?: 0} Recenzii",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        //userul nu a dat review
                        if (!token.isNullOrEmpty() && !userHasReviewed) {
                            Text(
                                text = "Lasa o recenzie:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Nota: ${selectedRating}/5", style = MaterialTheme.typography.bodyMedium)

                            Slider(
                                value = selectedRating.toFloat(),
                                onValueChange = { selectedRating = it.toInt() },
                                valueRange = 1f..5f,
                                steps = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                (1..5).forEach {
                                    Text(
                                        text = it.toString(),
                                        color = if (it == selectedRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Feedback:", style = MaterialTheme.typography.bodyMedium)

                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                placeholder = { Text("Spune-ne o parere...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ElevatedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        reviewStatus = try {
                                            val request = ReviewCreateModel(
                                                idObiectiv = obiectiv.idObiectiv,
                                                nota = selectedRating,
                                                comentariu = reviewComment
                                            )
                                            val response = ApiClient.reviewsApi.sendReview("Bearer $token", request)
                                            if (response.isSuccessful) {
                                                userHasReviewed = true
                                                showToast("Recenzie trimisa cu succes.")
                                            } else {
                                                "Eroare la trimiterea recenziei: ${response.errorBody()?.string()}"
                                            }
                                        } catch (e: Exception) {
                                            "Eroare de retea: ${e.message}"
                                        }.toString()
                                    }
                                },
                                enabled = reviewComment.length >= 5,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trimite")
                            }

                            reviewStatus?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        } else if (!token.isNullOrEmpty() && userHasReviewed) {
                            Text(
                                text = "✅ Deja ai dat o recenzie.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ObjectivePhotosSection(obiectiv.poze)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}