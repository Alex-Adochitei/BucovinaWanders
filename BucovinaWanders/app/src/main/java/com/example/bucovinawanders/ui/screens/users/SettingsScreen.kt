package com.example.bucovinawanders.ui.screens.users

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.navigation.*

import kotlinx.coroutines.*

import com.example.bucovinawanders.ui.theme.*
import com.example.bucovinawanders.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, isLoggedIn: Boolean, userName: String?, onLogout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())
    val pressedColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    val maxDistanceKm by DistancePreferences.getDistance(context).collectAsState(initial = 10f)
    var sliderPosition by remember { mutableFloatStateOf(maxDistanceKm) }

    LaunchedEffect(maxDistanceKm) {
        sliderPosition = maxDistanceKm
    }

    //UI pentru settings screen
    BucovinaWandersTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Setari", color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00A3E0))
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        if (isLoggedIn) {
                            Text(
                                text = "Bun-venit, $userName!",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        //switch pentru dark mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mod intunecat", modifier = Modifier.weight(1f))
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { newValue ->
                                    coroutineScope.launch {
                                        ThemePreferences.setTheme(context, newValue)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        var textValue by remember { mutableStateOf(sliderPosition.toInt().toString()) }

                        LaunchedEffect(sliderPosition) {
                            textValue = sliderPosition.toInt().toString()
                        }

                        Text(
                            text = "Distanta maxima pana la obiectivele turistice (km):",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.Start)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            //buton scadere
                            IconButton(onClick = {
                                val newVal = (sliderPosition - 1f).coerceAtLeast(1f)
                                sliderPosition = newVal
                                coroutineScope.launch {
                                    DistancePreferences.setDistance(context, newVal)
                                }
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            //camp numeric
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = {
                                    textValue = it
                                    it.toFloatOrNull()?.let { value ->
                                        if (value in 1f..100f) {
                                            sliderPosition = value
                                            coroutineScope.launch {
                                                DistancePreferences.setDistance(context, value)
                                            }
                                        }
                                    }
                                },
                                label = { Text("Distance (km)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                            )

                            //buton crestere
                            IconButton(onClick = {
                                val newVal = (sliderPosition + 1f).coerceAtMost(50f)
                                sliderPosition = newVal
                                coroutineScope.launch {
                                    DistancePreferences.setDistance(context, newVal)
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }

                        //buton pentru obiecgtivele vizitate vizibil doar daca utilizatorul este logat
                        if (isLoggedIn) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("visitedScreen") }, //navigare la visited screen
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Obiectivele turistice vizitate")
                                Icon(
                                    Icons.Default.RemoveRedEye,
                                    contentDescription = "Visited",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    //buton pentru login sau logout/delete account
                    Column {
                        //butoane disponibile unui user logat
                        if (isLoggedIn) {
                            //buton pentru logout, suntem redirectionati la maps screen
                            Button(
                                onClick = {
                                    onLogout()
                                    navController.navigate("mapsScreen") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Deconectare")
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            //buton pentru delete account, suntem redirectionati la delete account screen
                            Button(
                                onClick = { navController.navigate("deleteAccountScreen") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Sterge contul")
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            //buton de login, suntem redirectionati la login screen
                            Button(
                                onClick = { navController.navigate("loginScreen") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Autentifica-te")
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = "Login",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}