package com.vox.music.feature.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.vox.music.core.audio.model.PlayerState
import com.vox.music.ui.components.VoxCoverArt

@Composable
fun MiniPlayerBar(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(CircleShape)
            .background(Color(0xFF181818), CircleShape)
            .border(0.5.dp, Color(0xFF383838), CircleShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sisi Kiri: Circular Cover Art (46.dp)
            VoxCoverArt(
                filePath = track.filePath,
                contentDescription = track.title,
                shape = CircleShape,
                iconSize = 22.dp,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClick)
            )

            // Sisi Tengah: Judul Lagu & Artis (Area klik membuka PlayerScreen)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${track.artist} • ${playerState.formattedCurrentTime}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color(0xFFAAAAAA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sisi Kanan: Playback Controls Area (Isolated click events)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Lucide.SkipBack,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Lucide.Pause else Lucide.Play,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Lucide.SkipForward,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
