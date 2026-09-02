package com.vox.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vox.music.core.theme.repository.ThemeRepository
import com.vox.music.feature.library.LibraryScreen
import com.vox.music.feature.library.LibraryViewModel
import com.vox.music.feature.player.PlayerScreen
import com.vox.music.feature.player.PlayerViewModel
import com.vox.music.feature.player.components.MiniPlayerBar
import com.vox.music.ui.theme.VoxTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.compose.foundation.layout.navigationBarsPadding

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeConfig by themeRepository.themeStateFlow.collectAsStateWithLifecycle()

            VoxTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val libraryViewModel: LibraryViewModel = hiltViewModel()
                    val playerViewModel: PlayerViewModel = hiltViewModel()
                    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

                    var isPlayerExpanded by remember { mutableStateOf(false) }

                    BackHandler(enabled = isPlayerExpanded) {
                        isPlayerExpanded = false
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Library Content (Fills screen)
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onTrackSelected = { track, queueTracks ->
                                val startIndex = queueTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
                                playerViewModel.playTracks(queueTracks, startIndex)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Capsule Mini Player
                        AnimatedVisibility(
                            visible = playerState.currentTrack != null && !isPlayerExpanded,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        ) {
                            MiniPlayerBar(
                                playerState = playerState,
                                onTogglePlayPause = { playerViewModel.togglePlayPause() },
                                onSkipNext = { playerViewModel.skipNext() },
                                onSkipPrevious = { playerViewModel.skipPrevious() },
                                onClick = { isPlayerExpanded = true }
                            )
                        }

                        // Full Screen Player Overlay
                        AnimatedVisibility(
                            visible = isPlayerExpanded && playerState.currentTrack != null,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            PlayerScreen(
                                viewModel = playerViewModel,
                                onCollapse = { isPlayerExpanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
