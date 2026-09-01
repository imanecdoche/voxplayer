package com.vox.music.feature.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.core.lyrics.model.LyricsData
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

@Composable
fun LyricsScreen(
    track: AudioTrack,
    lyricsData: LyricsData,
    currentPositionMs: Long,
    activeLyricIndex: Int,
    onSeekTo: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    // Smooth auto-scroll when activeLyricIndex changes
    LaunchedEffect(activeLyricIndex, isAutoScrollEnabled) {
        if (isAutoScrollEnabled && activeLyricIndex >= 0 && activeLyricIndex < lyricsData.lines.size) {
            val targetScrollIndex = (activeLyricIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScrollIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${if (lyricsData.isSynced) "SYNCED LRC" else if (lyricsData.hasLyrics) "PLAIN TEXT" else "NO LYRICS"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VoxTheme.colors.subtleText
                )
            }

            if (lyricsData.isSynced) {
                Button(
                    onClick = { isAutoScrollEnabled = !isAutoScrollEnabled },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAutoScrollEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                        contentColor = if (isAutoScrollEnabled) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .border(0.5.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
                ) {
                    Text(
                        text = if (isAutoScrollEnabled) "FOLLOW ON" else "FOLLOW OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HairlineDivider()

        // Content
        when {
            !lyricsData.hasLyrics -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No lyrics found for this track.\nPlace a '${track.title}.lrc' file in the song folder\nor embed USLT lyrics via Tag Editor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VoxTheme.colors.subtleText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            lyricsData.isSynced -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 200.dp, start = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(lyricsData.lines) { index, line ->
                        val isActive = index == activeLyricIndex
                        val textColor by animateColorAsState(
                            targetValue = if (isActive) Color.White else VoxTheme.colors.subtleText,
                            animationSpec = tween(durationMillis = 200),
                            label = "lyricTextColor"
                        )

                        Text(
                            text = line.content.ifBlank { "• • •" },
                            style = if (isActive) {
                                MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 24.sp
                                )
                            },
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSeekTo(line.timestampMs)
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            else -> {
                // Plain text unsynced lyrics
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = lyricsData.plainText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
