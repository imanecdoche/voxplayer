package com.vox.music.core.audio.controller

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vox.music.core.audio.equalizer.EqualizerController
import com.vox.music.core.audio.equalizer.EqualizerState
import com.vox.music.core.audio.model.LoopMode
import com.vox.music.core.audio.model.PlayerState
import com.vox.music.core.audio.service.MusicPlaybackService
import com.vox.music.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val equalizerController: EqualizerController
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    val equalizerState: StateFlow<EqualizerState> = equalizerController.equalizerState

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private var currentPlaylist: List<AudioTrack> = emptyList()
    private val _currentQueue = MutableStateFlow<List<AudioTrack>>(emptyList())
    val currentQueue: StateFlow<List<AudioTrack>> = _currentQueue.asStateFlow()

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    private val _isSleepTimerEndOfTrack = MutableStateFlow(false)
    val isSleepTimerEndOfTrack: StateFlow<Boolean> = _isSleepTimerEndOfTrack.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller?.let { player ->
                setupPlayerListener(player)
                updateStateFromPlayer(player)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _playerState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                }
                if (_isSleepTimerEndOfTrack.value && playbackState == Player.STATE_ENDED) {
                    _isSleepTimerEndOfTrack.value = false
                    player.pause()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentTrack = findTrackFromMediaItem(mediaItem)
                _playerState.update {
                    it.copy(
                        currentTrack = currentTrack,
                        durationMs = player.duration.coerceAtLeast(0L),
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                    )
                }
                if (_isSleepTimerEndOfTrack.value && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    _isSleepTimerEndOfTrack.value = false
                    player.pause()
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val loopMode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> LoopMode.ONE
                    Player.REPEAT_MODE_ALL -> LoopMode.ALL
                    else -> LoopMode.NONE
                }
                _playerState.update { it.copy(loopMode = loopMode) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }
        })
    }

    private fun updateStateFromPlayer(player: Player) {
        val currentTrack = findTrackFromMediaItem(player.currentMediaItem)
        val loopMode = when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> LoopMode.ONE
            Player.REPEAT_MODE_ALL -> LoopMode.ALL
            else -> LoopMode.NONE
        }
        _playerState.update {
            it.copy(
                currentTrack = currentTrack,
                isPlaying = player.isPlaying,
                durationMs = player.duration.coerceAtLeast(0L),
                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                isShuffleEnabled = player.shuffleModeEnabled,
                loopMode = loopMode
            )
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let { player ->
                    val currentPos = player.currentPosition.coerceAtLeast(0L)
                    val duration = player.duration.coerceAtLeast(0L)

                    val a = _playerState.value.pointA
                    val b = _playerState.value.pointB
                    if (a != null && b != null && b > a && player.isPlaying) {
                        if (currentPos >= b || currentPos < a) {
                            player.seekTo(a)
                        }
                    }

                    _playerState.update {
                        it.copy(
                            currentPositionMs = currentPos,
                            durationMs = duration
                        )
                    }
                }
                delay(50)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        currentPlaylist = tracks
        _currentQueue.value = tracks
        val mediaItems = tracks.map { it.toMediaItem() }

        controller?.let { player ->
            player.setMediaItems(mediaItems, startIndex, 0L)
            player.prepare()
            val pitchFactor = PlayerState.semitonesToPitchFactor(_playerState.value.pitchSemitones)
            player.playbackParameters = androidx.media3.common.PlaybackParameters(_playerState.value.playbackSpeed, pitchFactor)
            player.play()
        }
    }

    fun playSingleTrack(track: AudioTrack) {
        playQueue(listOf(track), 0)
    }

    fun removeFromQueue(index: Int) {
        if (index !in currentPlaylist.indices) return
        val updated = currentPlaylist.toMutableList().apply { removeAt(index) }
        currentPlaylist = updated
        _currentQueue.value = updated
        controller?.removeMediaItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in currentPlaylist.indices || toIndex !in currentPlaylist.indices) return
        val updated = currentPlaylist.toMutableList()
        val item = updated.removeAt(fromIndex)
        updated.add(toIndex, item)
        currentPlaylist = updated
        _currentQueue.value = updated
        controller?.moveMediaItem(fromIndex, toIndex)
    }

    fun addToQueue(track: AudioTrack) {
        val updated = currentPlaylist + track
        currentPlaylist = updated
        _currentQueue.value = updated
        controller?.addMediaItem(track.toMediaItem())
    }

    fun clearQueueKeepCurrent() {
        val current = _playerState.value.currentTrack
        if (current != null) {
            currentPlaylist = listOf(current)
            _currentQueue.value = listOf(current)
            controller?.let { player ->
                val currentIndex = player.currentMediaItemIndex
                val totalItems = player.mediaItemCount
                if (totalItems > 1) {
                    for (i in (totalItems - 1) downTo (currentIndex + 1)) {
                        player.removeMediaItem(i)
                    }
                    for (i in (currentIndex - 1) downTo 0) {
                        player.removeMediaItem(i)
                    }
                }
            }
        } else {
            currentPlaylist = emptyList()
            _currentQueue.value = emptyList()
            controller?.clearMediaItems()
        }
    }

    fun skipToQueueIndex(index: Int) {
        if (index in currentPlaylist.indices) {
            controller?.seekToDefaultPosition(index)
        }
    }

    fun togglePlayPause() {
        controller?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun seekForward(intervalMs: Long = 10_000L) {
        controller?.let { player ->
            val currentPos = player.currentPosition.coerceAtLeast(0L)
            val duration = player.duration
            val maxLimit = if (duration > 0) duration else Long.MAX_VALUE
            val targetPos = (currentPos + intervalMs).coerceAtMost(maxLimit)
            seekTo(targetPos)
        }
    }

    fun seekBackward(intervalMs: Long = 10_000L) {
        controller?.let { player ->
            val currentPos = player.currentPosition.coerceAtLeast(0L)
            val targetPos = (currentPos - intervalMs).coerceAtLeast(0L)
            seekTo(targetPos)
        }
    }

    fun skipNext() {
        controller?.let { player ->
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
            } else if (currentPlaylist.size > 1) {
                player.seekToDefaultPosition(0)
            } else {
                player.seekTo(0L)
            }
        }
    }

    fun skipPrevious() {
        controller?.let { player ->
            val pos = player.currentPosition.coerceAtLeast(0L)
            if (pos <= 3000L) {
                if (player.hasPreviousMediaItem()) {
                    player.seekToPreviousMediaItem()
                } else if (currentPlaylist.size > 1) {
                    player.seekToDefaultPosition(currentPlaylist.size - 1)
                } else {
                    player.seekTo(0L)
                }
            } else {
                player.seekTo(0L)
            }
        }
    }

    fun updateTrackFavorite(trackId: Long, isFavorite: Boolean) {
        currentPlaylist = currentPlaylist.map {
            if (it.id == trackId) it.copy(isFavorite = isFavorite) else it
        }
        _currentQueue.update { queue ->
            queue.map { if (it.id == trackId) it.copy(isFavorite = isFavorite) else it }
        }
        _playerState.update { state ->
            if (state.currentTrack?.id == trackId) {
                state.copy(currentTrack = state.currentTrack.copy(isFavorite = isFavorite))
            } else {
                state
            }
        }
    }

    // ==================== SLEEP TIMER ====================

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return
        val totalMs = minutes * 60 * 1000L
        val targetEndTime = System.currentTimeMillis() + totalMs
        _sleepTimerRemainingMs.value = totalMs

        sleepTimerJob = scope.launch {
            while (isActive) {
                val remaining = targetEndTime - System.currentTimeMillis()
                if (remaining <= 0) {
                    _sleepTimerRemainingMs.value = null
                    controller?.pause()
                    break
                } else {
                    _sleepTimerRemainingMs.value = remaining
                }
                delay(500)
            }
        }
    }

    fun startSleepTimerEndOfTrack() {
        cancelSleepTimer()
        _isSleepTimerEndOfTrack.value = true
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMs.value = null
        _isSleepTimerEndOfTrack.value = false
    }

    fun toggleShuffle() {
        controller?.let { player ->
            val newShuffle = !player.shuffleModeEnabled
            player.shuffleModeEnabled = newShuffle
            _playerState.update { it.copy(isShuffleEnabled = newShuffle) }
        }
    }

    fun toggleLoopMode() {
        controller?.let { player ->
            val nextLoopMode = when (_playerState.value.loopMode) {
                LoopMode.NONE -> LoopMode.ALL
                LoopMode.ALL -> LoopMode.ONE
                LoopMode.ONE -> LoopMode.NONE
            }
            val repeatMode = when (nextLoopMode) {
                LoopMode.NONE -> Player.REPEAT_MODE_OFF
                LoopMode.ALL -> Player.REPEAT_MODE_ALL
                LoopMode.ONE -> Player.REPEAT_MODE_ONE
            }
            player.repeatMode = repeatMode
            _playerState.update { it.copy(loopMode = nextLoopMode) }
        }
    }

    fun setSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.00f)
        val rounded = (kotlin.math.round(clamped * 100) / 100f)
        _playerState.update { it.copy(playbackSpeed = rounded) }
        val pitchFactor = PlayerState.semitonesToPitchFactor(_playerState.value.pitchSemitones)
        controller?.playbackParameters = androidx.media3.common.PlaybackParameters(rounded, pitchFactor)
        val bundle = Bundle().apply {
            putFloat(MusicPlaybackService.EXTRA_SPEED, rounded)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_SPEED, Bundle.EMPTY),
            bundle
        )
    }

    fun incrementSpeed(delta: Float) {
        setSpeed(_playerState.value.playbackSpeed + delta)
    }

    fun resetSpeed() {
        setSpeed(1.0f)
    }

    fun setPitch(semitones: Int) {
        val clamped = semitones.coerceIn(-12, 12)
        _playerState.update { it.copy(pitchSemitones = clamped) }
        val pitchFactor = PlayerState.semitonesToPitchFactor(clamped)
        controller?.playbackParameters = androidx.media3.common.PlaybackParameters(_playerState.value.playbackSpeed, pitchFactor)
        val bundle = Bundle().apply {
            putInt(MusicPlaybackService.EXTRA_PITCH_SEMITONES, clamped)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_PITCH, Bundle.EMPTY),
            bundle
        )
    }

    fun incrementPitch(delta: Int) {
        setPitch(_playerState.value.pitchSemitones + delta)
    }

    fun resetPitch() {
        setPitch(0)
    }

    fun resetAllDsp() {
        resetSpeed()
        resetPitch()
    }

    fun setPointA() {
        val currentPos = controller?.currentPosition ?: 0L
        _playerState.update { it.copy(pointA = currentPos) }
        val bundle = Bundle().apply {
            putLong(MusicPlaybackService.EXTRA_POSITION_MS, currentPos)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_POINT_A, Bundle.EMPTY),
            bundle
        )
    }

    fun setPointB() {
        val currentPos = controller?.currentPosition ?: 0L
        val a = _playerState.value.pointA
        if (a != null && currentPos > a) {
            _playerState.update { it.copy(pointB = currentPos) }
            val bundle = Bundle().apply {
                putLong(MusicPlaybackService.EXTRA_POSITION_MS, currentPos)
            }
            controller?.sendCustomCommand(
                SessionCommand(MusicPlaybackService.ACTION_SET_POINT_B, Bundle.EMPTY),
                bundle
            )
        }
    }

    fun clearABLoop() {
        _playerState.update { it.copy(pointA = null, pointB = null) }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_CLEAR_AB_LOOP, Bundle.EMPTY),
            Bundle.EMPTY
        )
    }

    // ==================== EQUALIZER & AUDIO EFFECTS ====================

    fun toggleEqualizer(enabled: Boolean) {
        equalizerController.setEnabled(enabled)
        val bundle = Bundle().apply {
            putBoolean(MusicPlaybackService.EXTRA_EQ_ENABLED, enabled)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_EQ_ENABLED, Bundle.EMPTY),
            bundle
        )
    }

    fun setEqualizerBandLevel(bandIndex: Short, levelMb: Short) {
        equalizerController.setBandLevel(bandIndex, levelMb)
        val bundle = Bundle().apply {
            putShort(MusicPlaybackService.EXTRA_EQ_BAND_INDEX, bandIndex)
            putShort(MusicPlaybackService.EXTRA_EQ_BAND_LEVEL, levelMb)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_EQ_BAND, Bundle.EMPTY),
            bundle
        )
    }

    fun applyEqualizerPreset(presetName: String) {
        equalizerController.applyPreset(presetName)
        val bundle = Bundle().apply {
            putString(MusicPlaybackService.EXTRA_EQ_PRESET_NAME, presetName)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_APPLY_EQ_PRESET, Bundle.EMPTY),
            bundle
        )
    }

    fun setBassBoost(strength: Short) {
        equalizerController.setBassBoost(strength)
        val bundle = Bundle().apply {
            putShort(MusicPlaybackService.EXTRA_EQ_STRENGTH, strength)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_BASS_BOOST, Bundle.EMPTY),
            bundle
        )
    }

    fun setVirtualizer(strength: Short) {
        equalizerController.setVirtualizer(strength)
        val bundle = Bundle().apply {
            putShort(MusicPlaybackService.EXTRA_EQ_STRENGTH, strength)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_VIRTUALIZER, Bundle.EMPTY),
            bundle
        )
    }

    fun setLoudnessGain(gainMb: Int) {
        equalizerController.setLoudnessGain(gainMb)
        val bundle = Bundle().apply {
            putInt(MusicPlaybackService.EXTRA_EQ_GAIN_MB, gainMb)
        }
        controller?.sendCustomCommand(
            SessionCommand(MusicPlaybackService.ACTION_SET_LOUDNESS, Bundle.EMPTY),
            bundle
        )
    }

    private fun findTrackFromMediaItem(mediaItem: MediaItem?): AudioTrack? {
        if (mediaItem == null) return null
        val mediaId = mediaItem.mediaId.toLongOrNull() ?: return null
        return currentPlaylist.find { it.id == mediaId }
    }

    private fun AudioTrack.toMediaItem(): MediaItem {
        val uri = Uri.fromFile(File(filePath))
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(uri)
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }
}
