package com.vox.music.core.audio.equalizer

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMb: Short, // e.g. -1500 (-15 dB)
    val maxLevelMb: Short, // e.g. +1500 (+15 dB)
    val currentLevelMb: Short
) {
    val centerFreqFormatted: String
        get() = if (centerFreqHz >= 1000) {
            val kHz = centerFreqHz / 1000f
            if (kHz % 1f == 0f) "%dkHz".format(kHz.toInt()) else "%.1fkHz".format(kHz)
        } else {
            "${centerFreqHz}Hz"
        }

    val currentGainDb: Float
        get() = currentLevelMb / 100f
}

data class EqualizerPreset(
    val name: String,
    val bandLevelsMb: List<Short>
)

data class EqualizerState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val currentPresetName: String = "Flat",
    val bassBoostStrength: Short = 0, // 0 - 1000 (0% - 100%)
    val virtualizerStrength: Short = 0, // 0 - 1000 (0% - 100%)
    val loudnessGainMb: Int = 0 // 0 - 1000 mB (0 - 10 dB)
)
