package com.example.bucovinawanders.utils

import android.content.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.core.net.toUri

@Composable
fun ContactInfoSection(contact: String) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    //extragem info de contact din stringul contact
    val phoneRegex = Regex("""Telefon:\s*(\d{7,})""")
    val emailRegex = Regex("""E-mail:\s*([\w.-]+@[\w.-]+\.\w{2,})""")
    val siteRegex = Regex("""Site:\s*(https?://\S+)""")

    val phone = phoneRegex.find(contact)?.groupValues?.get(1)
    val email = emailRegex.find(contact)?.groupValues?.get(1)
    val site = siteRegex.find(contact)?.groupValues?.get(1)

    val noContactAvailable = phone == null && email == null && site == null

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (noContactAvailable) {
            Text(
                text = "Informatii de contact indisponibile.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //telefon
        phone?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:$it".toUri())
                    context.startActivity(intent)
                }
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Telefon", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        //email
        email?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$it".toUri()
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        //eite
        site?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    uriHandler.openUri(it)
                }
            ) {
                Icon(Icons.Default.Language, contentDescription = "Site", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}