package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = SophisticatedPillBg,
    onPrimaryContainer = PurplePrimary,
    secondary = PurpleSecondary,
    onSecondary = Color.Black,
    tertiary = GreenConnected,
    background = SophisticatedBg,
    onBackground = TextPrimary,
    surface = SophisticatedCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SophisticatedPillBg,
    onSurfaceVariant = TextMuted,
    error = RoseError,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = CyanVariant,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    tertiary = EmeraldSuccess,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Slate800,
    error = RoseError,
    onError = Color.White
)

@Composable
fun EasyMSRTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Force high-contrast dark theme by default for EasyMSR hardware console look
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
