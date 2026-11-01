package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaColorScheme = darkColorScheme(
    primary = NovaCyanAccent,
    onPrimary = NovaDarkBackground,
    primaryContainer = NovaForestGreen,
    onPrimaryContainer = NovaCyanAccent,
    secondary = NovaGold,
    onSecondary = NovaDarkBackground,
    secondaryContainer = NovaCardGlassLight,
    onSecondaryContainer = NovaGoldLight,
    tertiary = NovaEmerald,
    background = NovaDarkBackground,
    onBackground = NovaTextPrimary,
    surface = NovaDarkSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaCardGlass,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaGlassBorder,
    outlineVariant = NovaGlassBorderSubtle
)

@Composable
fun NovaChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = NovaDarkBackground.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = NovaDarkBackground.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = NovaColorScheme,
        typography = Typography,
        content = content
    )
}
