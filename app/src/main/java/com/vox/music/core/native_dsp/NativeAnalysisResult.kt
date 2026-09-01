package com.vox.music.core.native_dsp

data class NativeAnalysisResult(
    val bpm: Float,
    val musicalKey: String,
    val confidence: Float
)
