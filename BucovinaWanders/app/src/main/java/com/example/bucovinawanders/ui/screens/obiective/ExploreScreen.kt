package com.example.bucovinawanders.ui.screens.obiective

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*

import coil.compose.*
import kotlinx.coroutines.*

import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(obiective: List<ObiectivTuristicModel>, onDismiss: () -> Unit, onObiectivClick: (ObiectivTuristicModel) -> Unit) {
    val context = LocalContext.current
    var sortedObiective by remember { mutableStateOf(obiective.shuffled()) } //afiseaza aleatoriu obiective in lista Explore
    var sortOption by remember { mutableStateOf("none") } //retine optiunea de sortare a obiectivelor
    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp)
        ) {
            //butoane sortare
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //sortare dupa nota
                Button(
                    onClick = {
                        sortOption = "nota"
                        sortedObiective = obiective.sortedByDescending { it.notaRecenzii ?: 0f }
                    },
                    shape = RoundedCornerShape(50),
                    colors = if (sortOption == "nota") {
                        //buton apasat
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A3E0),
                            contentColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF000000)
                        )
                    } else {
                        //buton neapasat
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDarkTheme) Color(0xFF2f2f2f) else Color(0xFFf0f0f0),
                            contentColor = if (isDarkTheme) Color(0xFFb0b0b0) else Color(0xff808080)
                        )
                    }
                ) {
                    Text("Nota")
                }

                //sortare dupa numar de recenzii
                Button(
                    onClick = {
                        sortOption = "recenzii"
                        sortedObiective = obiective.sortedByDescending { it.numarRecenzii ?: 0 }
                    },
                    shape = RoundedCornerShape(50),
                    colors = if (sortOption == "recenzii") {
                        //buton apasat
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xff00A3E0),
                            contentColor = if (isDarkTheme) Color(0xffffffff) else Color(0xff000000)
                        )
                    } else {
                        //buton neapasat
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDarkTheme) Color(0xFF2f2f2f) else Color(0xFFf0f0f0),
                            contentColor = if (isDarkTheme) Color(0xFFb0b0b0) else Color(0xff808080)
                        )
                    }
                ) {
                    Text("Numar recenzii")
                }

                //sortare dupa program
                Button(
                    onClick = {
                        sortOption = "open"
                    },
                    shape = RoundedCornerShape(50),
                    colors = if (sortOption == "open") {
                        //buton apasat
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xff00A3E0),
                            contentColor = if (isDarkTheme) Color(0xffffffff) else Color(0xff000000)
                        )
                    } else {
                        //buton neapasat
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDarkTheme) Color(0xFF2f2f2f) else Color(0xFFf0f0f0),
                            contentColor = if (isDarkTheme) Color(0xFFb0b0b0) else Color(0xff808080)
                        )
                    }
                ) {
                    Text("Deschis acum")
                }
            }

            //apelam API ca sa afisam doar obiectele deschise
            if (sortOption == "open") {
                LaunchedEffect(Unit) {
                    launch {
                        try {
                            val openObiective = ApiClient.obiectiveApi.getOpenObiective()
                            sortedObiective = openObiective
                        } catch (e: Exception) {
                            println("Error: ${e.message}")
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedObiective.size) { index ->
                    val obiectiv = sortedObiective[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .clickable { onObiectivClick(obiectiv) }
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            //imagine obiectiv
                            val imagineUrl = obiectiv.poze.firstOrNull()?.urlPoza
                            if (imagineUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imagineUrl),
                                    contentDescription = obiectiv.nume,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                //nume obiectiv
                                Text(
                                    text = obiectiv.nume,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                                //nota si recenzii obiectiv
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    //nota
                                    if (obiectiv.notaRecenzii != null) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Rating",
                                            tint = Color(0xffFFC107)
                                        )
                                        Text(
                                            text = "${obiectiv.notaRecenzii.twoDecimalPlaces()} ",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    //recenzii
                                    if (obiectiv.numarRecenzii != null) {
                                        Text(
                                            text = "(${obiectiv.numarRecenzii} Recenzii)",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}