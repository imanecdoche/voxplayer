package com.vox.music.core.audio.dsp

import androidx.annotation.OptIn
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.vox.music.core.audio.model.PlayerState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Singleton
class SonicAudioProcessorHolder @Inject constructor() {

    val processor: SonicAudioProcessor = SonicAudioProcessor()

    var speed: Float = 1.0f
        private set

    var pitchSemitones: Int = 0
        private set

    fun setSpeed(newSpeed: Float) {
        // Clamp and round to 2 decimal places (step 0.05x precision)
        val clamped = newSpeed.coerceIn(0.25f, 3.00f)
        val rounded = (clamped * 100).roundToInt() / 100f
        this.speed = rounded
        processor.setSpeed(rounded)
    }

    fun setPitchSemitones(semitones: Int) {
        val clamped = semitones.coerceIn(-12, 12)
        this.pitchSemitones = clamped
        val pitchMultiplier = PlayerState.semitonesToPitchFactor(clamped)
        processor.setPitch(pitchMultiplier)
    }

    fun resetSpeed() {
        setSpeed(1.0f)
    }

    fun resetPitch() {
        setPitchSemitones(0)
    }

    fun resetAll() {
        resetSpeed()
        resetPitch()
    }
}
