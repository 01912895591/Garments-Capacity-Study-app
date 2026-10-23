package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElegantDarkDarkBlue,
    secondary = ElegantDarkAccentBlue,
    tertiary = ElegantDarkAccentBlue,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    onPrimary = ElegantDarkTextMain,
    onSecondary = ElegantDarkTextHelper,
    onBackground = ElegantDarkTextMain,
    onSurface = ElegantDarkTextMain,
    surfaceVariant = ElegantDarkCardGrid,
    outline = ElegantDarkBorder
)

private val LightColorScheme = darkColorScheme( // Force dark theme elements here too
    primary = ElegantDarkDarkBlue,
    secondary = ElegantDarkAccentBlue,
    tertiary = ElegantDarkAccentBlue,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    onPrimary = ElegantDarkTextMain,
    onSecondary = ElegantDarkTextHelper,
    onBackground = ElegantDarkTextMain,
    onSurface = ElegantDarkTextMain,
    surfaceVariant = ElegantDarkCardGrid,
    outline = ElegantDarkBorder
)


@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep it light-focused by default because standard factory/IE grids look best on white/cool-gray
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
