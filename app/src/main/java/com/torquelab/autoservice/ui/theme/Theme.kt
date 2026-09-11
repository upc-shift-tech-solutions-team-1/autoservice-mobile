package com.torquelab.autoservice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AutoServiceBlue,
    onPrimary = Color.White,

    primaryContainer = AutoServiceBlueLight,
    onPrimaryContainer = Color.White,

    secondary = AutoServiceOrange,
    onSecondary = AutoServiceBlueDark,

    secondaryContainer = AutoServiceOrangeLight,
    onSecondaryContainer = AutoServiceBlueDark,

    tertiary = AutoServiceBlueLight,
    onTertiary = Color.White,

    background = AutoServiceBackgroundLight,
    onBackground = AutoServiceTextPrimaryLight,

    surface = AutoServiceSurfaceLight,
    onSurface = AutoServiceTextPrimaryLight,

    surfaceVariant = AutoServiceSurfaceVariantLight,
    onSurfaceVariant = AutoServiceTextSecondaryLight,

    error = AutoServiceError,
    onError = Color.White,

    outline = AutoServiceOutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = AutoServiceOrangeLight,
    onPrimary = AutoServiceBlueDark,

    primaryContainer = AutoServiceBlue,
    onPrimaryContainer = Color.White,

    secondary = AutoServiceOrange,
    onSecondary = AutoServiceBlueDark,

    secondaryContainer = AutoServiceOrangeDark,
    onSecondaryContainer = Color.White,

    tertiary = AutoServiceBlueLight,
    onTertiary = Color.White,

    background = AutoServiceBackgroundDark,
    onBackground = AutoServiceTextPrimaryDark,

    surface = AutoServiceSurfaceDark,
    onSurface = AutoServiceTextPrimaryDark,

    surfaceVariant = AutoServiceSurfaceVariantDark,
    onSurfaceVariant = AutoServiceTextSecondaryDark,

    error = AutoServiceErrorDark,
    onError = Color(0xFF690005),

    outline = AutoServiceOutlineDark
)

@Composable
fun AutoServiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}