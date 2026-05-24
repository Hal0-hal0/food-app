package com.gastro.nameet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light Color Scheme - consistent orange branding
private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = White,
    primaryContainer = LightOrange,
    onPrimaryContainer = DarkOrange,
    secondary = DarkOrange,
    onSecondary = White,
    secondaryContainer = LightOrange,
    onSecondaryContainer = DarkOrange,
    tertiary = LightOrange,
    onTertiary = Black,
    tertiaryContainer = LightOrange,
    onTertiaryContainer = DarkOrange,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray400,
    outlineVariant = Gray200,
    error = ErrorRed,
    onError = White,
    scrim = Black
)

// Dark Color Scheme - consistent orange branding
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = White,
    primaryContainer = DarkOrange,
    onPrimaryContainer = LightOrange,
    secondary = LightOrange,
    onSecondary = Black,
    secondaryContainer = DarkOrange,
    onSecondaryContainer = LightOrange,
    tertiary = LightOrange,
    onTertiary = Black,
    tertiaryContainer = DarkOrange,
    onTertiaryContainer = LightOrange,
    background = Color(0xFF1C1B1F),
    onBackground = White,
    surface = Color(0xFF2C2C2C),
    onSurface = White,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray400,
    outline = Gray500,
    outlineVariant = Gray700,
    error = ErrorRed,
    onError = White,
    scrim = White
)

@Composable
fun GastroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colors disabled to keep the orange brand identity consistent
    // regardless of the user's wallpaper on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoppinsTypography,
        content = content
    )
}
