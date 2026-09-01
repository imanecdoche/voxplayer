package com.vox.music.core.native_dsp

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeDspEngine @Inject constructor() {

    companion object {
        init {
            try {
                System.loadLibrary("vox-dsp")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    external fun initDspEngine(): Boolean
    external fun releaseDspEngine()
    external fun processAudioBuffer(pcmData: FloatArray, sampleRate: Int): Long
    external fun detectBpmAndKeyNative(pcmData: FloatArray, sampleRate: Int): NativeAnalysisResult?
    external fun decodeAudioFileNative(filePath: String, maxDurationSec: Int): FloatArray?
    external fun detectBpm(filePath: String): Float
    external fun detectMusicalKey(filePath: String): String
    external fun analyzeChordProgression(filePath: String): String
    external fun analyzeChordProgressionFromPcm(pcmData: FloatArray, sampleRate: Int): String
}
