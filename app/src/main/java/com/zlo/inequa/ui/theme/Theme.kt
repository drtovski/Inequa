package com.zlo.inequa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue900,
    onPrimary = SurfaceLight,
    secondary = Blue700,
    onSecondary = SurfaceLight,
    tertiary = Blue100,
    onTertiary = Blue900,
    background = Slate50,
    onBackground = Blue900,
    surface = SurfaceLight,
    onSurface = Blue900,
    surfaceVariant = SurfaceTintLight,
    onSurfaceVariant = Blue700,
    error = ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueDarkPrimary,
    onPrimary = Slate900,
    secondary = BlueDarkAccent,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = BlueDarkPrimary,
    surface = Slate800,
    onSurface = BlueDarkPrimary,
    surfaceVariant = Slate700,
    onSurfaceVariant = BlueDarkAccent,
    error = ErrorDark
)

@Composable
fun InequaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
