package com.example.bucovinawanders.ui.theme

import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*

import com.example.bucovinawanders.utils.*

@Composable
fun BucovinaWandersTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isDarkTheme by ThemePreferences.getTheme(context).collectAsState(initial = isSystemInDarkTheme())

    //definim culorile pentru modul intunecat
    val darkColors = darkColorScheme(
        primary = Color(0xFF32A0FF),
        onPrimary = Color(0xFFFFFFFF),
        surface = Color(0xFF403e3b),
        onSurface = Color(0xFFe0e0e0),
        onSurfaceVariant = Color(0xFFe0e0e0)
    )

    //definim culorile pentru modul luminos
    val lightColors = lightColorScheme(
        primary = Color(0xFF32A0FF),
        onPrimary = Color(0xFFFFFFFF),
        surface = Color(0xFFe0e0e0),
        onSurface = Color(0xFF403e3b),
        onSurfaceVariant = Color(0xFF403e3b)
    )

    //aplicam tema in functie de modul selectat
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColors else lightColors,
        typography = Typography(),
        content = content
    )
}