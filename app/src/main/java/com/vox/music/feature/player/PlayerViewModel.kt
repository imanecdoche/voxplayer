package com.vox.music.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vox.music.core.audio.chords.ChordTracker
import com.vox.music.core.audio.controller.MusicPlayerController
import com.vox.music.core.audio.model.PlayerState
import com.vox.music.core.lyrics.LrcParser
import com.vox.music.core.lyrics.model.LyricsData
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.ChordEvent
import com.vox.music.core.model.Playlist
import com.vox.music.core.storage.repository.AudioAnalysisRepository
import com.vox.music.core.storage.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: MusicPlayerController,
    private val audioRepository: AudioRepository,
    private val audioAnalysisRepository: AudioAnalysisRepository,
    private val lrcParser: LrcParser,
    private val chordTracker: ChordTracker
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerController.playerState
    val equalizerState = playerController.equalizerState
    val currentQueue: StateFlow<List<AudioTrack>> = playerController.currentQueue

    val allTracks: StateFlow<List<AudioTrack>> = audioRepository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = audioRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lyricsData = MutableStateFlow(LyricsData())
    val lyricsData: StateFlow<LyricsData> = _lyricsData.asStateFlow()

    private val _chordProgression = MutableStateFlow<List<ChordEvent>>(emptyList())
    val chordProgression: StateFlow<List<ChordEvent>> = _chordProgression.asStateFlow()

    private var lastLoadedTrackPath: String? = null

    val activeLyricIndex: StateFlow<Int> = combine(playerState, _lyricsData) { state, lyrics ->
        if (!lyrics.isSynced || lyrics.lines.isEmpty()) {
            -1
        } else {
            lrcParser.findActiveLyricIndex(lyrics.lines, state.currentPositionMs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val activeChord: StateFlow<ChordEvent?> = combine(playerState, _chordProgression) { state, chords ->
        chordTracker.findActiveChord(chords, state.currentPositionMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val upcomingChords: StateFlow<List<ChordEvent>> = combine(playerState, _chordProgression) { state, chords ->
        chordTracker.getUpcomingChords(chords, state.currentPositionMs, maxCount = 4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            playerState.collect { state ->
                val track = state.currentTrack
                if (track != null && track.filePath != lastLoadedTrackPath) {
                    lastLoadedTrackPath = track.filePath
                    loadLyrics(track)
                    analyzeBpmAndKeyIfNeeded(track)
                    loadChordProgression(track)
                }
            }
        }
    }

    private fun loadLyrics(track: AudioTrack) {
        viewModelScope.launch {
            val metadataResult = audioRepository.readMetadata(track.filePath)
            val embeddedLyrics = metadataResult.getOrNull()?.lyrics
            val parsed = lrcParser.loadLyricsForAudioFile(track.filePath, embeddedLyrics)
            _lyricsData.value = parsed
        }
    }

    private fun analyzeBpmAndKeyIfNeeded(track: AudioTrack) {
        if (track.bpm == null || track.musicalKey == null) {
            viewModelScope.launch {
                audioAnalysisRepository.analyzeTrack(track)
            }
        }
    }

    private fun loadChordProgression(track: AudioTrack) {
        viewModelScope.launch {
            val chords = audioAnalysisRepository.getChordProgression(track)
            _chordProgression.value = chords
        }
    }

    fun toggleEqualizer(enabled: Boolean) {
        playerController.toggleEqualizer(enabled)
    }

    fun setEqualizerBandLevel(bandIndex: Short, levelMb: Short) {
        playerController.setEqualizerBandLevel(bandIndex, levelMb)
    }

    fun applyEqualizerPreset(presetName: String) {
        playerController.applyEqualizerPreset(presetName)
    }

    fun setBassBoost(strength: Short) {
        playerController.setBassBoost(strength)
    }

    fun setVirtualizer(strength: Short) {
        playerController.setVirtualizer(strength)
    }

    fun setLoudnessGain(gainMb: Int) {
        playerController.setLoudnessGain(gainMb)
    }

    fun playTracks(tracks: List<AudioTrack>, startIndex: Int) {
        playerController.playQueue(tracks, startIndex)
    }

    fun playSingleTrack(track: AudioTrack) {
        playerController.playSingleTrack(track)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun seekForward(intervalMs: Long = 10_000L) {
        playerController.seekForward(intervalMs)
    }

    fun seekBackward(intervalMs: Long = 10_000L) {
        playerController.seekBackward(intervalMs)
    }

    fun skipNext() {
        playerController.skipNext()
    }

    fun skipPrevious() {
        playerController.skipPrevious()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleLoopMode() {
        playerController.toggleLoopMode()
    }

    fun setSpeed(speed: Float) {
        playerController.setSpeed(speed)
    }

    fun incrementSpeed(delta: Float) {
        playerController.incrementSpeed(delta)
    }

    fun resetSpeed() {
        playerController.resetSpeed()
    }

    fun setPitch(semitones: Int) {
        playerController.setPitch(semitones)
    }

    fun incrementPitch(delta: Int) {
        playerController.incrementPitch(delta)
    }

    fun resetPitch() {
        playerController.resetPitch()
    }

    fun resetAllDsp() {
        playerController.resetAllDsp()
    }

    fun setPointA() {
        playerController.setPointA()
    }

    fun setPointB() {
        playerController.setPointB()
    }

    fun clearABLoop() {
        playerController.clearABLoop()
    }

    fun toggleFavorite(trackId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            audioRepository.toggleFavorite(trackId, isFavorite)
        }
    }

    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playerController.moveQueueItem(fromIndex, toIndex)
    }

    fun addToQueue(track: AudioTrack) {
        playerController.addToQueue(track)
    }

    fun skipToQueueIndex(index: Int) {
        playerController.skipToQueueIndex(index)
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            audioRepository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun createPlaylist(name: String, initialTrackId: Long? = null) {
        viewModelScope.launch {
            val id = audioRepository.createPlaylist(name)
            if (initialTrackId != null && id > 0) {
                audioRepository.addTrackToPlaylist(id, initialTrackId)
            }
        }
    }
}
