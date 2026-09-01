package com.vox.music.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.Repeat1
import com.composables.icons.lucide.Shuffle
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.Sliders
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vox.music.core.audio.model.LoopMode
import com.vox.music.feature.equalizer.EqualizerScreen
import com.vox.music.feature.player.components.SpeedPitchSheet
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.SharpCoverArt
import com.vox.music.ui.theme.VoxTheme

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    val track = playerState.currentTrack ?: return

    val lyricsData by viewModel.lyricsData.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()
    val activeChord by viewModel.activeChord.collectAsStateWithLifecycle()
    val upcomingChords by viewModel.upcomingChords.collectAsStateWithLifecycle()

    var showSpeedPitchSheet by remember { mutableStateOf(false) }
    var showEqualizerScreen by remember { mutableStateOf(false) }
    var showLyricsScreen by remember { mutableStateOf(false) }

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    if (showLyricsScreen) {
        com.vox.music.feature.lyrics.LyricsScreen(
            track = track,
            lyricsData = lyricsData,
            currentPositionMs = playerState.currentPositionMs,
            activeLyricIndex = activeLyricIndex,
            onSeekTo = { viewModel.seekTo(it) },
            onNavigateBack = { showLyricsScreen = false }
        )
        return
    }

    if (showEqualizerScreen) {
        EqualizerScreen(
            equalizerState = equalizerState,
            builtInPresets = listOf("Flat", "Bass Boost", "Treble Boost", "Vocal", "Acoustic", "Rock", "Electronic", "Custom"),
            onToggleEnabled = { viewModel.toggleEqualizer(it) },
            onSetBandLevel = { band, level -> viewModel.setEqualizerBandLevel(band, level) },
            onApplyPreset = { viewModel.applyEqualizerPreset(it) },
            onSetBassBoost = { viewModel.setBassBoost(it) },
            onSetVirtualizer = { viewModel.setVirtualizer(it) },
            onSetLoudness = { viewModel.setLoudnessGain(it) },
            onNavigateBack = { showEqualizerScreen = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = "Collapse Player",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showLyricsScreen = true }) {
                    Icon(
                        imageVector = Lucide.FileText,
                        contentDescription = "Lyrics",
                        tint = if (lyricsData.hasLyrics) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = { showEqualizerScreen = true }) {
                    Icon(
                        imageVector = Lucide.Sliders,
                        contentDescription = "Equalizer",
                        tint = if (equalizerState.isEnabled) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = { viewModel.toggleFavorite(track.id, !track.isFavorite) }) {
                    Icon(
                        imageVector = Lucide.Heart,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // 1:1 Album Cover Art (Sharp Corners, zero radius)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
        ) {
            SharpCoverArt(
                model = track.filePath,
                contentDescription = track.title,
                size = 360.dp,
                modifier = Modifier.matchParentSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Track Title & Artist Metadata
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${track.artist} — ${track.album}",
                style = MaterialTheme.typography.bodyMedium,
                color = VoxTheme.colors.subtleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Technical Metadata Badges (Monochrome)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bpmText = if (track.bpm != null) "${track.bpm.toInt()} BPM" else "BPM: --"
            val keyText = if (track.musicalKey != null) "KEY: ${track.musicalKey}" else "KEY: --"
            val formatText = track.mimeType.substringAfterLast('/').uppercase()

            Text(
                text = "[ $bpmText ]",
                style = MaterialTheme.typography.labelSmall,
                color = VoxTheme.colors.subtleText
            )
            Text(
                text = "[ $keyText ]",
                style = MaterialTheme.typography.labelSmall,
                color = VoxTheme.colors.subtleText
            )
            Text(
                text = "[ $formatText ]",
                style = MaterialTheme.typography.labelSmall,
                color = VoxTheme.colors.subtleText
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-time Chord Visualizer
        com.vox.music.feature.player.components.ChordVisualizer(
            activeChord = activeChord,
            upcomingChords = upcomingChords
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Scrubber Slider with A-B Marker Info
        Column(modifier = Modifier.fillMaxWidth()) {
            val sliderValue = if (isSeeking) seekPosition else playerState.currentPositionMs.toFloat()
            val maxRange = playerState.durationMs.coerceAtLeast(1L).toFloat()

            // Scrubber Timeline Slider
            Slider(
                value = sliderValue.coerceIn(0f, maxRange),
                onValueChange = {
                    isSeeking = true
                    seekPosition = it
                },
                onValueChangeFinished = {
                    viewModel.seekTo(seekPosition.toLong())
                    isSeeking = false
                },
                valueRange = 0f..maxRange,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground,
                    inactiveTrackColor = VoxTheme.colors.divider
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // A-B Loop Visual Marker Badges
            if (playerState.pointA != null || playerState.pointB != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val aTime = if (playerState.pointA != null) "POINT A: %02d:%02d".format((playerState.pointA!! / 1000) / 60, (playerState.pointA!! / 1000) % 60) else ""
                    val bTime = if (playerState.pointB != null) "POINT B: %02d:%02d".format((playerState.pointB!! / 1000) / 60, (playerState.pointB!! / 1000) % 60) else ""

                    Text(
                        text = aTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = bTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playerState.formattedCurrentTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = VoxTheme.colors.subtleText
                )
                Text(
                    text = playerState.formattedRemainingTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = VoxTheme.colors.subtleText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    imageVector = Lucide.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.isShuffleEnabled) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous
            IconButton(onClick = { viewModel.skipPrevious() }) {
                Icon(
                    imageVector = Lucide.SkipBack,
                    contentDescription = "Previous Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Play / Pause
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Lucide.Pause else Lucide.Play,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Next
            IconButton(onClick = { viewModel.skipNext() }) {
                Icon(
                    imageVector = Lucide.SkipForward,
                    contentDescription = "Next Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Loop Mode
            IconButton(onClick = { viewModel.toggleLoopMode() }) {
                val loopIcon = when (playerState.loopMode) {
                    LoopMode.ONE -> Lucide.Repeat1
                    else -> Lucide.Repeat
                }
                val loopTint = when (playerState.loopMode) {
                    LoopMode.NONE -> VoxTheme.colors.subtleText
                    else -> MaterialTheme.colorScheme.onBackground
                }
                Icon(
                    imageVector = loopIcon,
                    contentDescription = "Loop Mode",
                    tint = loopTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        HairlineDivider()

        // DSP & A-B Looper Footer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSpeedPitchSheet = true }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SPEED: %.2fx".format(playerState.playbackSpeed),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "PITCH: ${if (playerState.pitchSemitones > 0) "+" else ""}${playerState.pitchSemitones} st",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (playerState.isABLoopActive) "A-B: ACTIVE" else "A-B: OFF",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (playerState.isABLoopActive) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showSpeedPitchSheet) {
        SpeedPitchSheet(
            speed = playerState.playbackSpeed,
            pitchSemitones = playerState.pitchSemitones,
            pointA = playerState.pointA,
            pointB = playerState.pointB,
            onSpeedChange = { viewModel.setSpeed(it) },
            onPitchChange = { viewModel.setPitch(it) },
            onSetPointA = { viewModel.setPointA() },
            onSetPointB = { viewModel.setPointB() },
            onClearABLoop = { viewModel.clearABLoop() },
            onResetSpeed = { viewModel.resetSpeed() },
            onResetPitch = { viewModel.resetPitch() },
            onResetAll = { viewModel.resetAllDsp() },
            onDismiss = { showSpeedPitchSheet = false }
        )
    }
}
