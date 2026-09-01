package com.vox.music.core.audio.service

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.vox.music.MainActivity
import com.vox.music.core.audio.dsp.SonicAudioProcessorHolder
import com.vox.music.core.audio.equalizer.EqualizerController
import com.vox.music.feature.lockscreen.LockscreenPlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService() {

    @Inject
    lateinit var sonicHolder: SonicAudioProcessorHolder

    @Inject
    lateinit var equalizerController: EqualizerController

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var screenStateReceiver: BroadcastReceiver? = null

    // A-B Looping Points (in milliseconds)
    var pointA: Long? = null
    var pointB: Long? = null

    private val handler = Handler(Looper.getMainLooper())
    private val abLoopRunnable = object : Runnable {
        override fun run() {
            player?.let { p ->
                val b = pointB
                val a = pointA
                if (b != null && a != null && b > a && p.isPlaying) {
                    val currentPos = p.currentPosition
                    if (currentPos >= b || currentPos < a) {
                        p.seekTo(a)
                    }
                }
            }
            handler.postDelayed(this, 40)
        }
    }

    companion object {
        const val ACTION_SET_SPEED = "com.vox.music.ACTION_SET_SPEED"
        const val ACTION_SET_PITCH = "com.vox.music.ACTION_SET_PITCH"
        const val ACTION_SET_POINT_A = "com.vox.music.ACTION_SET_POINT_A"
        const val ACTION_SET_POINT_B = "com.vox.music.ACTION_SET_POINT_B"
        const val ACTION_CLEAR_AB_LOOP = "com.vox.music.ACTION_CLEAR_AB_LOOP"

        const val ACTION_SET_EQ_ENABLED = "com.vox.music.ACTION_SET_EQ_ENABLED"
        const val ACTION_SET_EQ_BAND = "com.vox.music.ACTION_SET_EQ_BAND"
        const val ACTION_APPLY_EQ_PRESET = "com.vox.music.ACTION_APPLY_EQ_PRESET"
        const val ACTION_SET_BASS_BOOST = "com.vox.music.ACTION_SET_BASS_BOOST"
        const val ACTION_SET_VIRTUALIZER = "com.vox.music.ACTION_SET_VIRTUALIZER"
        const val ACTION_SET_LOUDNESS = "com.vox.music.ACTION_SET_LOUDNESS"

        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_PITCH_SEMITONES = "extra_pitch_semitones"
        const val EXTRA_POSITION_MS = "extra_position_ms"

        const val EXTRA_EQ_ENABLED = "extra_eq_enabled"
        const val EXTRA_EQ_BAND_INDEX = "extra_eq_band_index"
        const val EXTRA_EQ_BAND_LEVEL = "extra_eq_band_level"
        const val EXTRA_EQ_PRESET_NAME = "extra_eq_preset_name"
        const val EXTRA_EQ_STRENGTH = "extra_eq_strength"
        const val EXTRA_EQ_GAIN_MB = "extra_eq_gain_mb"
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        registerScreenReceiver()
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
        }
        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    checkAndLaunchLockscreenPlayer(context)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenStateReceiver, filter)
        }
    }

    private fun checkAndLaunchLockscreenPlayer(context: Context) {
        val p = player ?: return
        if (!p.isPlaying) return
        val prefs = context.getSharedPreferences("vox_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("lockscreen_player_enabled", true)
        if (!isEnabled) return

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager?.isKeyguardLocked == true) {
            val lockIntent = Intent(context, LockscreenPlayerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(lockIntent)
        }
    }

    private fun initializePlayer() {
        // Build DefaultAudioSink with SonicAudioProcessor injected and AudioTrackPlaybackParams disabled
        // so that SonicAudioProcessor handles all speed and pitch shifts seamlessly
        val audioSink = DefaultAudioSink.Builder(this)
            .setAudioProcessors(arrayOf<AudioProcessor>(sonicHolder.processor))
            .setEnableAudioTrackPlaybackParams(false)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: android.content.Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return audioSink
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true) // Handles audio focus automatically
            .setHandleAudioBecomingNoisy(true) // Pauses automatically on headphone disconnect
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val sessionId = player?.audioSessionId ?: 0
                    if (sessionId > 0) {
                        equalizerController.attachToSession(sessionId)
                    }
                }
            }
        })

        // Attach initial audio session
        val initialSessionId = player?.audioSessionId ?: 0
        if (initialSessionId > 0) {
            equalizerController.attachToSession(initialSessionId)
        }

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(CustomMediaSessionCallback())
            .build()

        // Start A-B loop check ticker
        handler.post(abLoopRunnable)
    }

    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_SET_SPEED -> {
                    val speed = args.getFloat(EXTRA_SPEED, 1.0f)
                    sonicHolder.setSpeed(speed)
                    val pitchFactor = com.vox.music.core.audio.model.PlayerState.semitonesToPitchFactor(sonicHolder.pitchSemitones)
                    player?.playbackParameters = androidx.media3.common.PlaybackParameters(speed, pitchFactor)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_PITCH -> {
                    val semitones = args.getInt(EXTRA_PITCH_SEMITONES, 0)
                    sonicHolder.setPitchSemitones(semitones)
                    val pitchFactor = com.vox.music.core.audio.model.PlayerState.semitonesToPitchFactor(semitones)
                    player?.playbackParameters = androidx.media3.common.PlaybackParameters(sonicHolder.speed, pitchFactor)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_POINT_A -> {
                    val pos = args.getLong(EXTRA_POSITION_MS, player?.currentPosition ?: 0L)
                    pointA = pos
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_POINT_B -> {
                    val pos = args.getLong(EXTRA_POSITION_MS, player?.currentPosition ?: 0L)
                    if (pointA != null && pos > pointA!!) {
                        pointB = pos
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_CLEAR_AB_LOOP -> {
                    pointA = null
                    pointB = null
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_EQ_ENABLED -> {
                    val enabled = args.getBoolean(EXTRA_EQ_ENABLED, false)
                    equalizerController.setEnabled(enabled)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_EQ_BAND -> {
                    val band = args.getShort(EXTRA_EQ_BAND_INDEX, 0)
                    val level = args.getShort(EXTRA_EQ_BAND_LEVEL, 0)
                    equalizerController.setBandLevel(band, level)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_APPLY_EQ_PRESET -> {
                    val presetName = args.getString(EXTRA_EQ_PRESET_NAME, "Flat")
                    equalizerController.applyPreset(presetName)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_BASS_BOOST -> {
                    val strength = args.getShort(EXTRA_EQ_STRENGTH, 0)
                    equalizerController.setBassBoost(strength)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_VIRTUALIZER -> {
                    val strength = args.getShort(EXTRA_EQ_STRENGTH, 0)
                    equalizerController.setVirtualizer(strength)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_SET_LOUDNESS -> {
                    val gainMb = args.getInt(EXTRA_EQ_GAIN_MB, 0)
                    equalizerController.setLoudnessGain(gainMb)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        screenStateReceiver?.let {
            unregisterReceiver(it)
            screenStateReceiver = null
        }
        handler.removeCallbacks(abLoopRunnable)
        equalizerController.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
