package com.example.bucovinawanders.utils

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

import coil.compose.*
import kotlin.collections.*

import com.example.bucovinawanders.models.obiective.*

@Composable
fun ObjectivePhotosSection(photos: List<PozeObiectivModel>) {
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    //verifica daca lista de poze nu este goala
    if (photos.isNotEmpty()) {
        Text(
            text = "Photos",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        //lista orizontala de poze
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            //itereaza prin fiecare poza
            items(photos.size) { index ->
                //card care contine imaginea
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .width(180.dp)
                        .height(120.dp)
                        .clickable { selectedPhotoUrl = photos[index].urlPoza } //seteaza poza selectata cand se da click
                ) {
                    //imaginea afisata in card
                    Image(
                        painter = rememberAsyncImagePainter(photos[index].urlPoza), //incarca poza de la URL
                        contentDescription = "Objective Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    //daca exista o poza selectata afiseaza fullscreen intr-un dialog
    if (selectedPhotoUrl != null) {
        Dialog(onDismissRequest = { selectedPhotoUrl = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { selectedPhotoUrl = null },
                contentAlignment = Alignment.Center
            ) {
                //afiseaza imaginea selectata pe tot ecranul
                Image(
                    painter = rememberAsyncImagePainter(selectedPhotoUrl),
                    contentDescription = "Imagine",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}