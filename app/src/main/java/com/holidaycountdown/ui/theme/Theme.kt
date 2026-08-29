package com.holidaycountdown.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColors = darkColorScheme(
    primary = Color(0xFF745BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF30246D),
    onPrimaryContainer = Color(0xFFF7F4FF),
    secondary = Color(0xFFFF62D0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF672451),
    onSecondaryContainer = Color(0xFFFFF2FB),
    tertiary = Color(0xFF7C5CFF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF33276B),
    onTertiaryContainer = Color(0xFFF5F1FF),
    background = Color(0xFF060716),
    surface = Color(0xFF11142E),
    surfaceVariant = Color(0xFF202442),
    onBackground = Color(0xFFF4F3FF),
    onSurface = Color(0xFFF4F3FF),
    onSurfaceVariant = Color(0xFFD8DBF5),
    outline = Color(0xFF858CAB),
    outlineVariant = Color(0xFF444A69),
    inverseSurface = Color(0xFFF0EEFF),
    inverseOnSurface = Color(0xFF17172A),
    inversePrimary = Color(0xFF4F37D7)
)

@Composable fun HolidayCountdownTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CyberColors, content = content)
}