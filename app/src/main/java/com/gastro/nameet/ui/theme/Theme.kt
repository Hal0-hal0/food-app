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

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    secondary = LightOrange,
    tertiary = TertiaryDarkBlue,
    background = White,
    surface = White,
    error = ErrorRed,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = White,
    onBackground = Black,
    onSurface = Black,
    onError = White,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    scrim = Black
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = LightOrange,
    secondary = DarkOrange,
    tertiary = TertiaryDarkBlue,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2C2C2C),
    error = ErrorRed,
    onPrimary = Black,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White,
    onError = White,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray400,
    scrim = White
)

@Composable
fun GastroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Android 12+ dynamic colors
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
