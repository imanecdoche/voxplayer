package com.vox.music.core.storage.repository

import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.ChordEvent
import com.vox.music.core.native_dsp.NativeAnalysisResult

interface AudioAnalysisRepository {
    suspend fun analyzeTrack(track: AudioTrack): Result<NativeAnalysisResult>
    suspend fun analyzePendingTracks(tracks: List<AudioTrack>)
    suspend fun getChordProgression(track: AudioTrack): List<ChordEvent>
}
