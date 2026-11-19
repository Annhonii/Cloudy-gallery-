package com.annhonii.cloudygallery.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Nothing OS inspired monochrome palette
private val NothingBlack = Color(0xFF000000)
private val NothingWhite = Color(0xFFFFFFFF)
private val NothingGray10 = Color(0xFF1A1A1A)
private val NothingGray20 = Color(0xFF2A2A2A)
private val NothingGray30 = Color(0xFF3A3A3A)
private val NothingGray50 = Color(0xFF7A7A7A)
private val NothingGray70 = Color(0xFFB0B0B0)
private val NothingGray90 = Color(0xFFE0E0E0)
private val NothingAccent = Color(0xFFCCCCCC)
private val NothingError = Color(0xFFCF6679)
private val NothingErrorContainer = Color(0xFF3A0010)

val DarkColorScheme = darkColorScheme(
    primary = NothingWhite,
    onPrimary = NothingBlack,
    primaryContainer = NothingGray20,
    onPrimaryContainer = NothingWhite,
    secondary = NothingGray70,
    onSecondary = NothingBlack,
    secondaryContainer = NothingGray20,
    onSecondaryContainer = NothingGray90,
    tertiary = NothingAccent,
    onTertiary = NothingBlack,
    tertiaryContainer = NothingGray30,
    onTertiaryContainer = NothingWhite,
    error = NothingError,
    onError = NothingBlack,
    errorContainer = NothingErrorContainer,
    onErrorContainer = Color(0xFFFFB3C1),
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingBlack,
    onSurface = NothingWhite,
    surfaceVariant = NothingGray10,
    onSurfaceVariant = NothingGray70,
    outline = NothingGray30,
    outlineVariant = NothingGray20,
    scrim = Color(0x80000000),
    inverseSurface = NothingWhite,
    inverseOnSurface = NothingBlack,
    inversePrimary = NothingGray20,
    surfaceDim = Color(0xFF0A0A0A),
    surfaceBright = NothingGray10,
    surfaceContainerLowest = NothingBlack,
    surfaceContainerLow = Color(0xFF0F0F0F),
    surfaceContainer = NothingGray10,
    surfaceContainerHigh = NothingGray20,
    surfaceContainerHighest = NothingGray30,
)

val LightColorScheme = lightColorScheme(
    primary = NothingBlack,
    onPrimary = NothingWhite,
    primaryContainer = NothingGray90,
    onPrimaryContainer = NothingBlack,
    secondary = NothingGray50,
    onSecondary = NothingWhite,
    secondaryContainer = NothingGray90,
    onSecondaryContainer = NothingBlack,
    tertiary = NothingGray30,
    onTertiary = NothingWhite,
    tertiaryContainer = NothingGray70,
    onTertiaryContainer = NothingBlack,
    error = NothingError,
    onError = NothingWhite,
    errorContainer = Color(0xFFFFDADE),
    onErrorContainer = Color(0xFF3A0010),
    background = NothingWhite,
    onBackground = NothingBlack,
    surface = NothingWhite,
    onSurface = NothingBlack,
    surfaceVariant = NothingGray90,
    onSurfaceVariant = NothingGray30,
    outline = NothingGray50,
    outlineVariant = NothingGray70,
    scrim = Color(0x80000000),
    inverseSurface = NothingBlack,
    inverseOnSurface = NothingWhite,
    inversePrimary = NothingGray70,
)

@Composable
fun CloudyGalleryTheme(
    darkTheme: Boolean = true, // Always dark by default for Nothing OS feel
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CloudyTypography,
        shapes = CloudyShapes,
        content = content
    )
}
