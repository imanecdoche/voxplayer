package com.vox.music.core.storage.repository

import com.vox.music.core.audio.chords.ChordTracker
import com.vox.music.core.database.dao.AudioAnalysisDao
import com.vox.music.core.database.entity.AudioAnalysisCacheEntity
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.ChordEvent
import com.vox.music.core.native_dsp.AudioSignalAnalyzer
import com.vox.music.core.native_dsp.NativeAnalysisResult
import com.vox.music.core.native_dsp.NativeDspEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioAnalysisRepositoryImpl @Inject constructor(
    private val audioSignalAnalyzer: AudioSignalAnalyzer,
    private val nativeDspEngine: NativeDspEngine,
    private val audioAnalysisDao: AudioAnalysisDao,
    private val chordTracker: ChordTracker
) : AudioAnalysisRepository {

    private val analysisScope = CoroutineScope(Dispatchers.Default)

    override suspend fun analyzeTrack(track: AudioTrack): Result<NativeAnalysisResult> = withContext(Dispatchers.Default) {
        audioSignalAnalyzer.analyzeAudioTrack(track)
    }

    override suspend fun analyzePendingTracks(tracks: List<AudioTrack>) = withContext(Dispatchers.Default) {
        val unanalyzed = tracks.filter { it.bpm == null || it.musicalKey == null }
        analysisScope.launch {
            for (track in unanalyzed) {
                audioSignalAnalyzer.analyzeAudioTrack(track)
            }
        }
        Unit
    }

    override suspend fun getChordProgression(track: AudioTrack): List<ChordEvent> = withContext(Dispatchers.Default) {
        val cached = audioAnalysisDao.getCacheByTrackId(track.id)
        if (cached?.chordProgressionJson != null && cached.chordProgressionJson.isNotBlank()) {
            return@withContext chordTracker.parseChordJson(cached.chordProgressionJson)
        }

        // Compute via native C++
        val json = nativeDspEngine.analyzeChordProgression(track.filePath)
        if (json.isNotBlank() && json != "[]") {
            val cacheEntity = AudioAnalysisCacheEntity(
                trackId = track.id,
                filePath = track.filePath,
                bpm = track.bpm?.toDouble(),
                musicalKey = track.musicalKey,
                chordProgressionJson = json
            )
            audioAnalysisDao.insertOrUpdate(cacheEntity)
            return@withContext chordTracker.parseChordJson(json)
        }

        emptyList()
    }
}
