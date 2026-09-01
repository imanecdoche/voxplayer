package com.vox.music.feature.library.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MoreVertical
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxCoverArt
import com.vox.music.ui.theme.VoxTheme

@Composable
fun TrackListItem(
    track: AudioTrack,
    onClick: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onMoreOptions: () -> Unit = {},
    isCurrentTrack: Boolean = false,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art with 6.dp rounded corners and Lucide.Music fallback + Playing Waveform overlay
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                VoxCoverArt(
                    filePath = track.filePath,
                    contentDescription = track.title,
                    shape = RoundedCornerShape(6.dp),
                    iconSize = 20.dp,
                    modifier = Modifier.size(44.dp)
                )

                if (isCurrentTrack && isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingWaveformBars(modifier = Modifier.height(20.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ${track.durationFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                    if (track.bpm != null) {
                        Text(
                            text = " • ${track.bpm.toInt()} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (track.musicalKey != null) {
                        Text(
                            text = " • ${track.musicalKey}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                if (track.customTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.customTags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Favorite Heart Icon with pastel red #FF6B6B when active
            IconButton(
                onClick = { onToggleFavorite(!track.isFavorite) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Lucide.Heart,
                    contentDescription = if (track.isFavorite) "Unfavorite" else "Favorite",
                    tint = if (track.isFavorite) Color(0xFFFF6B6B) else VoxTheme.colors.subtleText,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onMoreOptions,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Lucide.MoreVertical,
                    contentDescription = "More Actions",
                    tint = VoxTheme.colors.subtleText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HairlineDivider()
    }
}

@Composable
private fun PlayingWaveformBars(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, delayMillis = 80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 40, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar1Height.dp)
                .background(Color.White, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar2Height.dp)
                .background(Color.White, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar3Height.dp)
                .background(Color.White, RoundedCornerShape(1.dp))
        )
    }
}

