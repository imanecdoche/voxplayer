package com.vox.music.core.audio.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")
@Singleton
class EqualizerController @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    private var currentSessionId: Int = 0

    val builtInPresets: List<String> = listOf(
        "Flat",
        "Bass Boost",
        "Treble Boost",
        "Vocal",
        "Acoustic",
        "Rock",
        "Electronic",
        "Custom"
    )

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(_equalizerState.value.bassBoostStrength)
                    enabled = _equalizerState.value.isEnabled && _equalizerState.value.bassBoostStrength > 0
                }
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(_equalizerState.value.virtualizerStrength)
                    enabled = _equalizerState.value.isEnabled && _equalizerState.value.virtualizerStrength > 0
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    setTargetGain(_equalizerState.value.loudnessGainMb)
                    enabled = _equalizerState.value.isEnabled && _equalizerState.value.loudnessGainMb > 0
                }
            }

            readBandsFromDevice()
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide fallback dummy bands if hardware effects fail
            setupFallbackBands()
        }
    }

    private fun readBandsFromDevice() {
        val eq = equalizer ?: run {
            setupFallbackBands()
            return
        }

        try {
            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]

            val bandsList = mutableListOf<EqualizerBand>()
            for (i in 0 until numBands) {
                val bandIndex = i.toShort()
                val centerFreq = eq.getCenterFreq(bandIndex) / 1000 // mHz to Hz
                val currentLevel = eq.getBandLevel(bandIndex)

                bandsList.add(
                    EqualizerBand(
                        index = bandIndex,
                        centerFreqHz = centerFreq,
                        minLevelMb = minLevel,
                        maxLevelMb = maxLevel,
                        currentLevelMb = currentLevel
                    )
                )
            }

            _equalizerState.update { it.copy(bands = bandsList) }
        } catch (e: Exception) {
            e.printStackTrace()
            setupFallbackBands()
        }
    }

    private fun setupFallbackBands() {
        val defaultFrequencies = listOf(60, 230, 910, 3600, 14000)
        val bands = defaultFrequencies.mapIndexed { index, freq ->
            EqualizerBand(
                index = index.toShort(),
                centerFreqHz = freq,
                minLevelMb = -1500,
                maxLevelMb = 1500,
                currentLevelMb = 0
            )
        }
        _equalizerState.update { it.copy(bands = bands) }
    }

    fun setEnabled(enabled: Boolean) {
        _equalizerState.update { it.copy(isEnabled = enabled) }
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled && _equalizerState.value.bassBoostStrength > 0
            virtualizer?.enabled = enabled && _equalizerState.value.virtualizerStrength > 0
            loudnessEnhancer?.enabled = enabled && _equalizerState.value.loudnessGainMb > 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Short, levelMb: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMb)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _equalizerState.update { state ->
            val updatedBands = state.bands.map { band ->
                if (band.index == bandIndex) band.copy(currentLevelMb = levelMb) else band
            }
            state.copy(bands = updatedBands, currentPresetName = "Custom")
        }
    }

    fun applyPreset(presetName: String) {
        val bands = _equalizerState.value.bands
        if (bands.isEmpty()) return

        val count = bands.size
        val levels = when (presetName) {
            "Bass Boost" -> calculateCurve(count, doubleArrayOf(6.0, 4.5, 1.0, 0.0, 0.0))
            "Treble Boost" -> calculateCurve(count, doubleArrayOf(0.0, 0.0, 1.0, 4.5, 6.0))
            "Vocal" -> calculateCurve(count, doubleArrayOf(-1.5, 1.5, 5.0, 3.0, -1.0))
            "Acoustic" -> calculateCurve(count, doubleArrayOf(3.5, 1.5, 0.0, 2.5, 3.5))
            "Rock" -> calculateCurve(count, doubleArrayOf(4.5, 2.0, -1.0, 2.5, 4.5))
            "Electronic" -> calculateCurve(count, doubleArrayOf(5.5, 3.0, 0.0, 2.0, 5.0))
            "Flat" -> calculateCurve(count, doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0))
            else -> return
        }

        levels.forEachIndexed { index, gainDb ->
            val levelMb = (gainDb * 100).toInt().toShort()
            val bandIndex = index.toShort()
            try {
                equalizer?.setBandLevel(bandIndex, levelMb)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _equalizerState.update { state ->
            val updatedBands = state.bands.mapIndexed { index, band ->
                val gainDb = levels.getOrElse(index) { 0.0 }
                band.copy(currentLevelMb = (gainDb * 100).toInt().toShort())
            }
            state.copy(bands = updatedBands, currentPresetName = presetName)
        }
    }

    private fun calculateCurve(numBands: Int, reference5Band: DoubleArray): List<Double> {
        if (numBands == 5) return reference5Band.toList()
        // Interpolate across custom band count
        return (0 until numBands).map { i ->
            val pos = i.toDouble() / (numBands - 1).coerceAtLeast(1) * 4.0
            val idx = pos.toInt().coerceIn(0, 3)
            val frac = pos - idx
            reference5Band[idx] * (1.0 - frac) + reference5Band[idx + 1] * frac
        }
    }

    fun setBassBoost(strength: Short) {
        val clamped = strength.coerceIn(0, 1000)
        _equalizerState.update { it.copy(bassBoostStrength = clamped) }
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(clamped)
                    it.enabled = _equalizerState.value.isEnabled && clamped > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVirtualizer(strength: Short) {
        val clamped = strength.coerceIn(0, 1000)
        _equalizerState.update { it.copy(virtualizerStrength = clamped) }
        try {
            virtualizer?.let {
                if (it.strengthSupported) {
                    it.setStrength(clamped)
                    it.enabled = _equalizerState.value.isEnabled && clamped > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        val clamped = gainMb.coerceIn(0, 1000)
        _equalizerState.update { it.copy(loudnessGainMb = clamped) }
        try {
            loudnessEnhancer?.let {
                it.setTargetGain(clamped)
                it.enabled = _equalizerState.value.isEnabled && clamped > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        currentSessionId = 0
    }
}
