package com.vox.music.core.native_dsp

import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioSignalAnalyzer @Inject constructor(
    private val nativeDspEngine: NativeDspEngine,
    private val audioTrackDao: AudioTrackDao
) {

    suspend fun analyzeAudioTrack(track: AudioTrack, sampleRate: Int = 44100): Result<NativeAnalysisResult> = withContext(Dispatchers.Default) {
        try {
            // Decode first 30 seconds for fast analysis
            val pcm = nativeDspEngine.decodeAudioFileNative(track.filePath, maxDurationSec = 30)
            if (pcm == null || pcm.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to decode audio PCM buffer via NDK"))
            }

            val result = nativeDspEngine.detectBpmAndKeyNative(pcm, sampleRate)
                ?: return@withContext Result.failure(Exception("Signal analysis returned null"))

            // Update database
            if (result.bpm > 0 && result.musicalKey.isNotBlank()) {
                audioTrackDao.updateBpmAndKey(
                    trackId = track.id,
                    bpm = result.bpm.toDouble(),
                    musicalKey = result.musicalKey
                )
            }

            Result.success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
