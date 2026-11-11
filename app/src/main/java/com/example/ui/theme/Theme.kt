package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val InjahniaDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = CarbonBlack,
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = OnElectricCyanContainer,
    secondary = MachinistSky,
    onSecondary = CarbonBlack,
    secondaryContainer = MachinistSkyContainer,
    onSecondaryContainer = WorkshopWhite,
    tertiary = WarningAmber,
    onTertiary = CarbonBlack,
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = WorkshopWhite,
    background = GraphiteDarkest,
    onBackground = WorkshopWhite,
    surface = GraphiteSurface,
    onSurface = WorkshopWhite,
    surfaceVariant = GraphiteSurfaceVariant,
    onSurfaceVariant = WorkshopSlate,
    outline = GraphiteBorder,
    error = CriticalRed,
    onError = WorkshopWhite,
    errorContainer = CriticalRedContainer,
    onErrorContainer = WorkshopWhite
)

private val InjahniaLightColorScheme = lightColorScheme(
    primary = ElectricBlueDeep,
    onPrimary = Color.White,
    primaryContainer = ElectricCyanLightContainer,
    onPrimaryContainer = Color(0xFF00363D),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = WarningAmber,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun InjahniaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InjahniaDarkColorScheme else InjahniaLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
