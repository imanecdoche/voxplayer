package com.vox.music.feature.lockscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.Repeat1
import com.composables.icons.lucide.Shuffle
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.vox.music.core.audio.model.LoopMode
import com.vox.music.core.audio.model.PlayerState
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.VoxCoverArt
import com.vox.music.ui.components.VoxSlider
import com.vox.music.ui.theme.VoxTheme

@Composable
fun LockscreenPlayerScreen(
    track: AudioTrack,
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleLoopMode: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row: Dismiss / Minimize Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = "Dismiss Lockscreen Player",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "VOX LITE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = VoxTheme.colors.subtleText
            )

            Spacer(modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Cover Art
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            VoxCoverArt(
                filePath = track.filePath,
                contentDescription = track.title,
                shape = RoundedCornerShape(16.dp),
                iconSize = 56.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metadata: Title & Artist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Thin Scrubber (2dp height) & Time Counter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val sliderValue = if (isSeeking) seekPosition else playerState.currentPositionMs.toFloat()
            val maxRange = playerState.durationMs.coerceAtLeast(1L).toFloat()

            VoxSlider(
                value = sliderValue.coerceIn(0f, maxRange),
                onValueChange = {
                    isSeeking = true
                    seekPosition = it
                },
                onValueChangeFinished = {
                    onSeekTo(seekPosition.toLong())
                    isSeeking = false
                },
                valueRange = 0f..maxRange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playerState.formattedCurrentTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF888888)
                )
                Text(
                    text = playerState.formattedTotalDuration,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF888888)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Controls Row: 5 Clean Monochrome Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Shuffle
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Lucide.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.isShuffleEnabled) Color.White else Color(0xFF555555),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Previous Track
            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Lucide.SkipBack,
                    contentDescription = "Previous Track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. Play / Pause
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Lucide.Pause else Lucide.Play,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 4. Next Track
            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Lucide.SkipForward,
                    contentDescription = "Next Track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 5. Loop Mode
            IconButton(
                onClick = onToggleLoopMode,
                modifier = Modifier.size(42.dp)
            ) {
                val loopIcon = when (playerState.loopMode) {
                    LoopMode.ONE -> Lucide.Repeat1
                    else -> Lucide.Repeat
                }
                val loopTint = when (playerState.loopMode) {
                    LoopMode.NONE -> Color(0xFF555555)
                    else -> Color.White
                }
                Icon(
                    imageVector = loopIcon,
                    contentDescription = "Loop Mode",
                    tint = loopTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Swipe / Dismiss Indicator
        Text(
            text = "SWIPE UP OR TAP ARROW TO DISMISS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
