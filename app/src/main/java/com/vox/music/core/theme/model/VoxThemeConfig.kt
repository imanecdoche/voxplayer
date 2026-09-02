package com.vox.music.core.theme.model

import androidx.compose.ui.graphics.Color

data class VoxThemeConfig(
    val version: Int = 1,
    val colors: ColorSchemeConfig = ColorSchemeConfig(),
    val dimensions: DimensionsConfig = DimensionsConfig(),
    val offsets: OffsetsConfig = OffsetsConfig(),
    val paddings: PaddingsConfig = PaddingsConfig(),
    val assets: AssetPathsConfig = AssetPathsConfig()
)

data class ColorSchemeConfig(
    val textPrimaryHex: String = "#FFFFFF",
    val textSecondaryHex: String = "#757575",
    val textSecondaryAlpha: Float = 1.0f,
    val backgroundHex: String = "#000000",
    val accentHex: String = "#FFFFFF",
    val capsuleBackgroundHex: String = "#1E1E1E",
    val capsuleTintHex: String = "#FFFFFF",
    val sliderTrackActiveHex: String = "#FFFFFF",
    val sliderTrackInactiveHex: String = "#333333",
    val sliderThumbHex: String = "#FFFFFF",
    val toggleActiveHex: String = "#FFFFFF",
    val toggleInactiveHex: String = "#333333",
    val appIconTintHex: String = "#FFFFFF",
    val defaultArtworkBgHex: String = "#1E1E1E",
    val defaultMusicIconHex: String = "#757575",
    val seekbarLineHex: String = "#FFFFFF"
) {
    val textPrimary: Color get() = textPrimaryHex.toComposeColor(Color.White)
    val textSecondary: Color get() = textSecondaryHex.toComposeColor(Color(0xFF757575)).copy(alpha = textSecondaryAlpha)
    val background: Color get() = backgroundHex.toComposeColor(Color.Black)
    val accent: Color get() = accentHex.toComposeColor(Color.White)
    val capsuleBackground: Color get() = capsuleBackgroundHex.toComposeColor(Color(0xFF1E1E1E))
    val capsuleTint: Color get() = capsuleTintHex.toComposeColor(Color.White)
    val sliderTrackActive: Color get() = sliderTrackActiveHex.toComposeColor(Color.White)
    val sliderTrackInactive: Color get() = sliderTrackInactiveHex.toComposeColor(Color(0xFF333333))
    val sliderThumb: Color get() = sliderThumbHex.toComposeColor(Color.White)
    val toggleActive: Color get() = toggleActiveHex.toComposeColor(Color.White)
    val toggleInactive: Color get() = toggleInactiveHex.toComposeColor(Color(0xFF333333))
    val appIconTint: Color get() = appIconTintHex.toComposeColor(Color.White)
    val defaultArtworkBg: Color get() = defaultArtworkBgHex.toComposeColor(Color(0xFF1E1E1E))
    val defaultMusicIcon: Color get() = defaultMusicIconHex.toComposeColor(Color(0xFF757575))
    val seekbarLine: Color get() = seekbarLineHex.toComposeColor(Color.White)
}

data class DimensionsConfig(
    val scaleGlobal: Float = 1.0f,
    val capsuleHeightDp: Int = 56,
    val capsuleCornerRadiusPercent: Int = 50,
    val artworkRadiusDp: Int = 28,
    val artworkSizeScale: Float = 1.0f,
    val playbackControlsScale: Float = 1.0f,
    val sliderThicknessDp: Int = 2,
    val sliderThumbDiameterDp: Int = 8,
    val toggleScale: Float = 1.0f,
    val appHeaderIconSizeDp: Int = 24,
    val defaultMusicIconSizeDp: Int = 36,
    val groupSpacingDp: Int = 16
)

data class OffsetsConfig(
    val capsuleOffsetY: Int = 12,
    val capsuleOffsetX: Int = 0,
    val artworkOffsetY: Int = 0,
    val seekbarOffsetY: Int = 0,
    val playbackControlsOffsetY: Int = 0,
    val appHeaderIconOffsetX: Int = 0,
    val defaultMusicIconOffsetY: Int = 0
)

data class PaddingsConfig(
    val screenHorizontalPaddingDp: Int = 16,
    val capsulePaddingHorizontalDp: Int = 16,
    val controlsGapDp: Int = 24
)

data class AssetPathsConfig(
    val homeBackgroundRelativePath: String? = null,
    val tracklistBackgroundRelativePath: String? = null,
    val playerBackgroundRelativePath: String? = null,
    val settingsBackgroundRelativePath: String? = null,
    val customHeaderIconRelativePath: String? = null,
    val customIconPackDirRelativePath: String? = null
)

// Extension helpers for Color <-> Hex conversion
fun String.toComposeColor(default: Color = Color.White): Color {
    return try {
        val cleanHex = this.trim().removePrefix("#")
        when (cleanHex.length) {
            6 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
            8 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
            3 -> {
                val expanded = cleanHex.map { "$it$it" }.joinToString("")
                Color(android.graphics.Color.parseColor("#$expanded"))
            }
            else -> default
        }
    } catch (e: Exception) {
        default
    }
}

fun Color.toHex(includeAlpha: Boolean = false): String {
    val a = (this.alpha * 255).toInt().coerceIn(0, 255)
    val r = (this.red * 255).toInt().coerceIn(0, 255)
    val g = (this.green * 255).toInt().coerceIn(0, 255)
    val b = (this.blue * 255).toInt().coerceIn(0, 255)
    return if (includeAlpha) {
        String.format("#%02X%02X%02X%02X", a, r, g, b)
    } else {
        String.format("#%02X%02X%02X", r, g, b)
    }
}
