package com.vox.music.feature.lockscreen

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vox.music.feature.player.PlayerViewModel
import com.vox.music.ui.theme.VoxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LockscreenPlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLockscreenFlags()
        enableEdgeToEdge()

        setContent {
            VoxTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -30f) { // Swipe up
                                    finish()
                                }
                            }
                        },
                    color = Color.Black
                ) {
                    val playerViewModel: PlayerViewModel = hiltViewModel()
                    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

                    // If no track is available or queue is empty, dismiss
                    LaunchedEffect(playerState.currentTrack) {
                        if (playerState.currentTrack == null) {
                            finish()
                        }
                    }

                    playerState.currentTrack?.let { track ->
                        LockscreenPlayerScreen(
                            track = track,
                            playerState = playerState,
                            onTogglePlayPause = { playerViewModel.togglePlayPause() },
                            onSkipNext = { playerViewModel.skipNext() },
                            onSkipPrevious = { playerViewModel.skipPrevious() },
                            onToggleShuffle = { playerViewModel.toggleShuffle() },
                            onToggleLoopMode = { playerViewModel.toggleLoopMode() },
                            onSeekTo = { playerViewModel.seekTo(it) },
                            onDismiss = { finish() }
                        )
                    }
                }
            }
        }
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
