package com.vox.music.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.vox.music.core.theme.model.VoxThemeConfig

@Immutable
data class VoxCustomColors(
    val divider: Color,
    val subtleText: Color,
    val markerAB: Color,
    val surfaceHighlight: Color
)

val LocalVoxTheme = staticCompositionLocalOf { VoxThemeConfig() }

val LocalVoxColors = staticCompositionLocalOf {
    VoxCustomColors(
        divider = DarkDivider,
        subtleText = NeutralGray,
        markerAB = DarkMarkerAB,
        surfaceHighlight = Color(0xFF111111)
    )
}

@Composable
fun VoxTheme(
    themeConfig: VoxThemeConfig = LocalVoxTheme.current,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColors = themeConfig.colors
    val colorScheme = darkColorScheme(
        primary = dynamicColors.accent,
        onPrimary = dynamicColors.background,
        primaryContainer = dynamicColors.capsuleBackground,
        onPrimaryContainer = dynamicColors.capsuleTint,
        secondary = dynamicColors.textSecondary,
        onSecondary = dynamicColors.textPrimary,
        background = dynamicColors.background,
        onBackground = dynamicColors.textPrimary,
        surface = dynamicColors.background,
        onSurface = dynamicColors.textPrimary,
        surfaceVariant = dynamicColors.capsuleBackground,
        onSurfaceVariant = dynamicColors.textSecondary,
        outline = dynamicColors.sliderTrackInactive,
        outlineVariant = dynamicColors.sliderTrackInactive
    )

    val customColors = VoxCustomColors(
        divider = dynamicColors.sliderTrackInactive,
        subtleText = dynamicColors.textSecondary,
        markerAB = dynamicColors.accent,
        surfaceHighlight = dynamicColors.capsuleBackground
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            // If background is very light, use dark status bar icons
            val bgLum = dynamicColors.background.red * 0.299f + dynamicColors.background.green * 0.587f + dynamicColors.background.blue * 0.114f
            insetsController.isAppearanceLightStatusBars = bgLum > 0.5f
            insetsController.isAppearanceLightNavigationBars = bgLum > 0.5f
        }
    }

    CompositionLocalProvider(
        LocalVoxTheme provides themeConfig,
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
    val themeConfig: VoxThemeConfig
        @Composable
        get() = LocalVoxTheme.current

    val colors: VoxCustomColors
        @Composable
        get() = LocalVoxColors.current
}
