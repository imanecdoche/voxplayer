package com.vox.music.ui.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

enum class VoxEasingCurve(val displayName: String, val description: String) {
    LINEAR("Linear", "Constant uniform speed"),
    EASE_IN("Ease-In", "Accelerates gradually from start"),
    EASE_OUT("Ease-Out", "Decelerates softly into resting place"),
    EASE_IN_OUT("Ease-In-Out", "Smooth acceleration and deceleration"),
    SINE_QUAD("Sine / Quad", "Gentle natural easing curve"),
    CUBIC_QUINT("Cubic / Quint", "Punchy modern responsive curve"),
    EXPO("Exponential", "Dramatic swift snap"),
    CIRC("Circular", "Smooth circular radial curve"),
    BOUNCE("Bounce", "Playful spring overshoot curve")
}

fun VoxEasingCurve.toComposeEasing(): Easing {
    return when (this) {
        VoxEasingCurve.LINEAR -> LinearEasing
        VoxEasingCurve.EASE_IN -> FastOutLinearInEasing
        VoxEasingCurve.EASE_OUT -> LinearOutSlowInEasing
        VoxEasingCurve.EASE_IN_OUT -> FastOutSlowInEasing
        VoxEasingCurve.SINE_QUAD -> CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
        VoxEasingCurve.CUBIC_QUINT -> CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
        VoxEasingCurve.EXPO -> CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
        VoxEasingCurve.CIRC -> CubicBezierEasing(0f, 0.55f, 0.45f, 1f)
        VoxEasingCurve.BOUNCE -> CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    }
}

fun <T> getGlobalAnimationSpec(
    durationMs: Int = 300,
    curve: VoxEasingCurve = VoxEasingCurve.EASE_IN_OUT
): AnimationSpec<T> {
    return tween(
        durationMillis = durationMs,
        easing = curve.toComposeEasing()
    )
}
