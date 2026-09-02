package com.vox.music.ui.components

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.dynamicMotionBlur(
    velocity: Float,
    intensity: Float = 5.0f,
    isEnabled: Boolean = true
): Modifier = if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && velocity > 0.02f) {
    // Map velocity (0..1) * intensity (1..10) to blur radius (0..25px)
    val blurRadius = (velocity * (intensity * 2.5f)).coerceIn(0f, 25f)
    if (blurRadius > 0.5f) {
        this.graphicsLayer {
            try {
                renderEffect = android.graphics.RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    android.graphics.Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            } catch (e: Exception) {
                // Safe fallback if GPU doesn't support RenderEffect
            }
        }
    } else {
        this
    }
} else {
    this
}
