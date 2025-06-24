package com.example.bucovinawanders.ui.screens.users

import android.widget.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import kotlinx.coroutines.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.models.users.*
import com.example.bucovinawanders.ui.theme.*
import com.example.bucovinawanders.utils.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SavesScreen(navController: NavController, token: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    //detectam tema sistemului
    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())
    val pressedColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var savedObjectives by remember { mutableStateOf<List<ObiectivTuristicModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    //functie pentru a afisa un toast
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //functie care navigheaza la ecranul de mapare
    val onObiectivClick: (ObiectivTuristicModel) -> Unit = { obiectiv ->
        navController.navigate("mapsScreen?obiectivId=${obiectiv.idObiectiv}")
    }

    //cand tokenul se modifica, incercam sa incarcam obiectivele salvate
    LaunchedEffect(token) {
        savedObjectives = emptyList()
        errorMessage = null
        isLoading = true

        if (token != null) {
            try {
                val response = ApiClient.savesApi.getSavedObiective("Bearer $token")
                if (response.isSuccessful) {
                    savedObjectives = response.body() ?: emptyList()
                } else {
                    errorMessage = "Nu ai niciun obiectiv turistic in lista de favorite."
                }
            } catch (e: Exception) {
                errorMessage = "Eroare de retea: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    //UI pentru ecranul de favorite
    BucovinaWandersTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Obiective turistice favorite", color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00A3E0))
                )
            }
        ) { padding -> //continutul principal al ecranului
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                //alegem ce afisam in functie de starea actuala a aplicatiei
                when {
                    token == null -> {
                        //daca userul nu e logat
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Nu esti autentificat.", style = MaterialTheme.typography.headlineSmall)

                            //navigare la ecranul de login
                            Button(
                                onClick = { navController.navigate("loginScreen") },
                                modifier = Modifier.padding(top = 24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = pressedColor,
                                    contentColor = textColor
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Login")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Autentifica-te")
                            }
                        }
                    }

                    //daca datele inca se incarca
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    //daca au aparut erori
                    errorMessage != null -> {
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    //daca nu avem niciun obiect salvat
                    savedObjectives.isEmpty() -> {
                        Text(
                            "Nu ai niciun obiectiv turistic in lista de favorite.",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        //daca avem obiectivuri salvate le afisam
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(savedObjectives, key = { it.idObiectiv }) { obiectiv ->
                                val dismissState = rememberDismissState()

                                //detectam actiunea de swipe pe fiecare obiectiv
                                LaunchedEffect(dismissState.currentValue) {
                                    if (dismissState.currentValue == DismissValue.DismissedToEnd) {
                                        //swipe spre dreapta - marcam ca vizitat
                                        if (token.isNotEmpty()) {
                                            coroutineScope.launch {
                                                try {
                                                    val request = UserVisitCreateModel(idObiectiv = obiectiv.idObiectiv)
                                                    val response = ApiClient.visitsApi.visitObiectiv(
                                                        "Bearer $token",
                                                        request
                                                    )
                                                    if (response.isSuccessful) {
                                                        showToast("Marcat ca vizitat.")
                                                    } else {
                                                        showToast("Eroare la marcare ca vizitat.")
                                                    }
                                                } catch (_: Exception) {
                                                    showToast("Eroare la marcare ca vizitat.")
                                                }
                                            }
                                        } else {
                                            showToast("Trebuie sa fii autentificat.")
                                        }
                                        dismissState.reset() //resetam ca sa ramana cardul pe ecran
                                    } else if (dismissState.currentValue == DismissValue.DismissedToStart) {
                                        //swipe spre stanga - stergem din lista de favorite
                                        if (token.isNotEmpty()) {
                                            coroutineScope.launch {
                                                try {
                                                    val deleteResponse = ApiClient.savesApi.deleteObiectiv(
                                                        "Bearer $token",
                                                        obiectiv.idObiectiv
                                                    )
                                                    if (deleteResponse.isSuccessful) {
                                                        //eliminam din lista
                                                        savedObjectives = savedObjectives.filter { it.idObiectiv != obiectiv.idObiectiv }
                                                    } else {
                                                        showToast("Eroare la stergere a obiectivului.")
                                                    }
                                                } catch (_: Exception) {
                                                    showToast("Eroare la stergere a obiectivului.")
                                                }
                                            }
                                        }
                                    }
                                }

                                //componente vizuale pentru fiecare obiectiv
                                SwipeToDismiss(
                                    state = dismissState,
                                    directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                                    background = {
                                        val direction = dismissState.dismissDirection

                                        val color = when (direction) {
                                            DismissDirection.StartToEnd -> Color(0xFF4CAF50) //vizitat
                                            DismissDirection.EndToStart -> Color(0xFFF44336) //sters
                                            else -> Color.Transparent
                                        }

                                        val icon = when (direction) {
                                            DismissDirection.StartToEnd -> Icons.Default.Visibility
                                            DismissDirection.EndToStart -> Icons.Default.Delete
                                            else -> null
                                        }

                                        val alignment = when (direction) {
                                            DismissDirection.StartToEnd -> Alignment.CenterStart
                                            DismissDirection.EndToStart -> Alignment.CenterEnd
                                            else -> Alignment.Center
                                        }

                                        //fundal care apare la swipe
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = alignment
                                        ) {
                                            icon?.let {
                                                Icon(
                                                    imageVector = it,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    },
                                    //cardul care contine informatiile despre obiectiv

                                    dismissContent = {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onObiectivClick(obiectiv) },
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = obiectiv.nume,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        text = obiectiv.descriere ?: "",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}