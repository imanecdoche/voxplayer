package com.vox.music.core.audio.model

import com.vox.music.core.model.AudioTrack
import kotlin.math.pow

enum class LoopMode {
    NONE,
    ALL,
    ONE
}

data class PlayerState(
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val pitchSemitones: Int = 0,
    val isShuffleEnabled: Boolean = false,
    val loopMode: LoopMode = LoopMode.NONE,
    val pointA: Long? = null,
    val pointB: Long? = null,
    val isBuffering: Boolean = false
) {
    val isABLoopActive: Boolean
        get() = pointA != null && pointB != null

    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedCurrentTime: String
        get() = formatTime(currentPositionMs)

    val formattedRemainingTime: String
        get() {
            val remaining = (durationMs - currentPositionMs).coerceAtLeast(0L)
            return "-${formatTime(remaining)}"
        }

    val formattedTotalDuration: String
        get() = formatTime(durationMs)

    companion object {
        fun semitonesToPitchFactor(semitones: Int): Float {
            return 2.0.pow(semitones.toDouble() / 12.0).toFloat()
        }

        private fun formatTime(ms: Long): String {
            val totalSeconds = (ms / 1000).coerceAtLeast(0L)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
