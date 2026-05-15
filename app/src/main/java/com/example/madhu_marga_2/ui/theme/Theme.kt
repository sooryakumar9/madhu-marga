package com.example.madhu_marga_2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EarthyGreen,
    onPrimary = WarmCream,
    primaryContainer = DeepOlive,
    onPrimaryContainer = DustyBeige,
    secondary = CaramelBrown,
    onSecondary = WarmCream,
    tertiary = HealthyLeaf,
    background = DarkForestBg,
    surface = DarkSurfaceOlive,
    onBackground = DarkTextCream,
    onSurface = DarkTextCream,
    surfaceVariant = DarkCardCocoa,
    onSurfaceVariant = DustyBeige,
    error = PestWarning,
    outline = EarthyGreen
)

private val LightColorScheme = lightColorScheme(
    primary = EarthyGreen,
    onPrimary = WarmCream,
    primaryContainer = SoftSage,
    onPrimaryContainer = TextDarkGreen,
    secondary = CaramelBrown,
    onSecondary = WarmCream,
    tertiary = HealthyLeaf,
    background = BackgroundOrganic,
    surface = SurfaceCream,
    onBackground = TextDarkGreen,
    onSurface = TextDarkGreen,
    surfaceVariant = CardBeige,
    onSurfaceVariant = TextMutedGreen,
    error = PestWarning,
    outline = EarthyGreen
)

@Composable
fun Madhumarga2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
