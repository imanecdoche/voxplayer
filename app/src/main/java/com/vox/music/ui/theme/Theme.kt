package com.vox.music.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class VoxCustomColors(
    val divider: Color,
    val subtleText: Color,
    val markerAB: Color,
    val surfaceHighlight: Color
)

val LocalVoxColors = staticCompositionLocalOf {
    VoxCustomColors(
        divider = DarkDivider,
        subtleText = NeutralGray,
        markerAB = DarkMarkerAB,
        surfaceHighlight = Color(0xFF111111)
    )
}

// Dark Mode Color Scheme (Default Amoled Black #000000)
private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = PureBlack,
    onPrimaryContainer = PureWhite,
    secondary = NeutralGray,
    onSecondary = PureWhite,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = PureBlack,
    onSurfaceVariant = NeutralGray,
    outline = DarkDivider,
    outlineVariant = DarkDivider
)

// Light Mode Color Scheme (Pure White #FFFFFF)
private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = PureWhite,
    onPrimaryContainer = PureBlack,
    secondary = NeutralGray,
    onSecondary = PureBlack,
    background = PureWhite,
    onBackground = PureBlack,
    surface = LightSurface,
    onSurface = PureBlack,
    surfaceVariant = PureWhite,
    onSurfaceVariant = NeutralGray,
    outline = LightDivider,
    outlineVariant = LightDivider
)

@Composable
fun VoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        VoxCustomColors(
            divider = DarkDivider,
            subtleText = NeutralGray,
            markerAB = DarkMarkerAB,
            surfaceHighlight = Color(0xFF111111)
        )
    } else {
        VoxCustomColors(
            divider = LightDivider,
            subtleText = NeutralGray,
            markerAB = LightMarkerAB,
            surfaceHighlight = Color(0xFFF5F5F5)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalVoxColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object VoxTheme {
    val colors: VoxCustomColors
        @Composable
        get() = LocalVoxColors.current
}
