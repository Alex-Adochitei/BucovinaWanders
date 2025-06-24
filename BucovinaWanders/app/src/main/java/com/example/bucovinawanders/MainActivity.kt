package com.example.bucovinawanders

import android.Manifest
import android.annotation.*
import android.content.pm.*
import android.os.*
import android.widget.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.activity.result.contract.*
import androidx.compose.runtime.*
import androidx.core.content.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.google.android.gms.maps.model.*

import com.example.bucovinawanders.ui.screens.*
import com.example.bucovinawanders.ui.screens.users.*
import com.example.bucovinawanders.ui.theme.*

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //cream un launcher pentru permisiunea de a folosi GPS
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(this, "Permission is needed!", Toast.LENGTH_LONG).show()
                }
            }

        //verificam daca avem permisiunea de a folosi GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        //shared preferences pentru autentificare
        val sharedPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        var isLoggedIn by mutableStateOf(sharedPrefs.getString("token", null) != null)
        var userName by mutableStateOf(sharedPrefs.getString("userName", "") ?: "")

        setContent {
            //retinem tokenul JWT in stare compose pentru a fi reactiv
            var token by remember(isLoggedIn) {
                mutableStateOf(sharedPrefs.getString("token", null))
            }

            var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

            BucovinaWandersTheme {
                val navController = rememberNavController() //controller de navigare

                //definim toate rutele aplicatiei
                NavHost(
                    navController,
                    startDestination = "mapsScreen"  //deschide mapa de inceput
                ) {
                    composable("mapsScreen") {
                        MapsScreen(
                            navController = navController,
                            token = token,
                            routePoints = routePoints,
                            onRoutePointsChange = { newPoints -> routePoints = newPoints }
                        )
                    }

                    composable("routeScreen") {
                        RouteScreen(
                            navController = navController,
                            token = token,
                            routePoints = routePoints,
                            onRoutePointsChange = { newPoints -> routePoints = newPoints },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    //harta cu un obiectiv selectat
                    composable(
                        route = "mapsScreen?obiectivId={obiectivId}",
                        arguments = listOf(navArgument("obiectivId") {
                            type = NavType.IntType
                            defaultValue = -1
                        })
                    ) {
                        val obiectivId = it.arguments?.getInt("obiectivId") ?: -1
                        MapsScreen(navController = navController, token = token, obiectivId = obiectivId)
                    }

                    //ecranul de setari
                    composable("settingsScreen") {
                        SettingsScreen(
                            navController,
                            isLoggedIn = isLoggedIn,
                            userName = userName,
                            onLogout = {
                                isLoggedIn = false //seteaza userul ca nelogat
                                sharedPrefs.edit { remove("token") }  //sterge token-ul dupa logout
                                navController.navigate("mapsScreen") {
                                    popUpTo("mapsScreen") { inclusive = true } //revenim la harta
                                }
                            }
                        )
                    }

                    //ecranul de login
                    composable("loginScreen") {
                        LoginScreen(
                            navController,
                            onLoginSuccess = { token, name ->  //daca loginul este reusit
                                isLoggedIn = true //seteaza userul ca logat
                                userName = name //salvam usernameul
                                sharedPrefs.edit {
                                    putString("token", token) //salvam tokenul
                                    putString("userName", name)  //salvam usernameul
                                }
                                navController.navigate("mapsScreen") {
                                    popUpTo("loginScreen") { inclusive = true } //revenim la harta
                                }
                            }
                        )
                    }

                    //ecranul de inregistrare
                    composable("authScreen") {
                        AuthScreen(navController)
                    }

                    //ecranul de obiective salvate
                    composable("savesScreen") {
                        SavesScreen(navController, token)
                    }

                    //ecranul de obiectiv vizitat
                    composable("visitedScreen") {
                        VisitedScreen(navController, token)
                    }

                    //ecranul de stergere a contului
                    composable("deleteAccountScreen") {
                        DeleteUserScreen(
                            navController,
                            onAccountDeleted = {
                                isLoggedIn = false //seteaza userul ca nelogat
                                sharedPrefs.edit { clear() }  //sterge toate datele de autentificare dupa stergere cont
                                navController.navigate("mapsScreen") {
                                    popUpTo("mapsScreen") { inclusive = true } //revenim la harta
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}