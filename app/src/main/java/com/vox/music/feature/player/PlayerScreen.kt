package com.vox.music.feature.player

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.composables.icons.lucide.Bluetooth
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.Repeat1
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shuffle
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.Sliders
import com.composables.icons.lucide.Tv
import com.vox.music.feature.player.components.AudioOutputBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import com.vox.music.core.audio.model.LoopMode
import com.vox.music.core.model.AudioMetadata
import com.vox.music.feature.equalizer.EqualizerScreen
import com.vox.music.feature.library.components.AddToPlaylistDialog
import com.vox.music.feature.metadata.AudioInspectorBottomSheet
import com.vox.music.feature.player.components.QueueBottomSheet
import com.vox.music.feature.player.components.SpeedPitchSheet
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxCoverArt
import com.vox.music.ui.components.VoxSlider
import com.vox.music.ui.theme.VoxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    val currentQueue by viewModel.currentQueue.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val sleepTimerRemainingMs by viewModel.sleepTimerRemainingMs.collectAsStateWithLifecycle()
    val isSleepTimerEndOfTrack by viewModel.isSleepTimerEndOfTrack.collectAsStateWithLifecycle()
    val audioRouteState by viewModel.audioRouteState.collectAsStateWithLifecycle()

    val track = playerState.currentTrack ?: return

    val lyricsData by viewModel.lyricsData.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()

    var showSpeedPitchSheet by remember { mutableStateOf(false) }
    var showEqualizerScreen by remember { mutableStateOf(false) }
    var showLyricsScreen by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showAudioOutputSheet by remember { mutableStateOf(false) }

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vox_prefs", Context.MODE_PRIVATE) }
    val dynamicBackgroundEnabled = remember(prefs) { prefs.getBoolean("dynamic_background_enabled", true) }
    val dynamicBgIntensity = remember(prefs) { prefs.getFloat("dynamic_background_intensity", 0.22f) }
    var dominantColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(track.filePath, dynamicBackgroundEnabled) {
        if (!dynamicBackgroundEnabled) {
            dominantColor = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(track.filePath)
                val picture = retriever.embeddedPicture
                retriever.release()
                if (picture != null) {
                    val bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.size)
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val dom = palette.getDominantColor(android.graphics.Color.TRANSPARENT)
                        if (dom != android.graphics.Color.TRANSPARENT) {
                            dominantColor = Color(dom)
                        } else {
                            val vibrant = palette.getVibrantColor(android.graphics.Color.TRANSPARENT)
                            dominantColor = if (vibrant != android.graphics.Color.TRANSPARENT) Color(vibrant) else null
                        }
                    }
                } else {
                    dominantColor = null
                }
            } catch (e: Exception) {
                dominantColor = null
            }
        }
    }

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

    // Vertical Drag Gesture to Dismiss Player
    var screenOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedScreenOffsetY by animateFloatAsState(targetValue = screenOffsetY, label = "screenDismissY")

    // Dynamic Pastel Aura on White Canvas with adjustable intensity
    val bgBrush = if (dominantColor != null && dynamicBackgroundEnabled) {
        val alphaPrimary = dynamicBgIntensity.coerceIn(0.05f, 0.95f)
        val alphaSecondary = (alphaPrimary * 0.35f).coerceIn(0.02f, 0.5f)
        Brush.radialGradient(
            colors = listOf(
                dominantColor!!.copy(alpha = alphaPrimary),
                dominantColor!!.copy(alpha = alphaSecondary),
                Color.White
            ),
            radius = 1200f
        )
    } else {
        SolidColor(Color.White)
    }

    // Horizontal Paging Carousel Queue Data
    val queueList = if (currentQueue.isNotEmpty()) currentQueue else listOf(track)
    val currentTrackIndex = queueList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = currentTrackIndex,
        pageCount = { queueList.size.coerceAtLeast(1) }
    )

    LaunchedEffect(track.id) {
        val targetIdx = queueList.indexOfFirst { it.id == track.id }
        if (targetIdx >= 0 && targetIdx != pagerState.currentPage) {
            pagerState.scrollToPage(targetIdx)
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress && pagerState.currentPage in queueList.indices) {
            val selectedTrack = queueList[pagerState.currentPage]
            if (selectedTrack.id != track.id) {
                viewModel.skipToQueueIndex(pagerState.currentPage)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .offset { IntOffset(0, animatedScreenOffsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (screenOffsetY > 220f) {
                            onCollapse()
                        }
                        screenOffsetY = 0f
                    },
                    onDragCancel = {
                        screenOffsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0 || screenOffsetY > 0) {
                            screenOffsetY = (screenOffsetY + dragAmount).coerceAtLeast(0f)
                        }
                    }
                )
            }
            .background(Color.White)
            .background(bgBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Navigation Bar (Kiri: ChevronDown, Kanan: Lyric Mic, Track Info, Config Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = "Collapse Player",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Kanan (Dynamic Bluetooth, Lyric Mic, Track Info, Config Settings)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 0. Dynamic Bluetooth Audio Routing Button (Shown ONLY if Bluetooth output is connected)
                if (audioRouteState.isBluetoothConnected) {
                    IconButton(
                        onClick = { showAudioOutputSheet = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.Bluetooth,
                            contentDescription = "Audio Output & Effects",
                            tint = if (!audioRouteState.isRoutingToSpeaker) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 1. Tombol Lyric (Ikon Mic)
                IconButton(
                    onClick = { showLyricsScreen = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Mic,
                        contentDescription = "Lyrics",
                        tint = if (lyricsData.hasLyrics) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 2. Tombol Track Info (Ikon Info)
                IconButton(
                    onClick = { showInfoSheet = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Info,
                        contentDescription = "Track Info & Audio Analysis",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 3. Tombol Config / DSP (Ikon Settings)
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Settings,
                        contentDescription = "DSP & Equalizer Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Center Art Cover with Horizontal Paging Carousel (Single centered artwork, zero peeking)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            pageSpacing = 24.dp
        ) { page ->
            val pageTrack = queueList.getOrNull(page) ?: track
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    VoxCoverArt(
                        filePath = pageTrack.filePath,
                        contentDescription = pageTrack.title,
                        shape = RoundedCornerShape(28.dp),
                        iconSize = 64.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Track Metadata (Center Aligned Title & Artist)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = VoxTheme.colors.subtleText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Action Toolbar Row (Queue Playlist, Favorite Like, Add to Saved Playlist)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Kiri (Current / Temporary Queue Playlist)
            IconButton(
                onClick = { showQueueSheet = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Lucide.ListMusic,
                    contentDescription = "Current Playing Queue",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tombol Tengah (Favorite / Like - pastel red #FF6B6B when active)
            IconButton(
                onClick = { viewModel.toggleFavorite(track.id, !track.isFavorite) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Lucide.Heart,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) Color(0xFFFF6B6B) else VoxTheme.colors.subtleText,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tombol Kanan (Add to Saved Playlist)
            IconButton(
                onClick = { showAddToPlaylistDialog = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = "Add to Saved Playlist",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Scrubber & Time Tracker with custom VoxSlider (2dp line, 12dp thumb)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
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
                    viewModel.seekTo(seekPosition.toLong())
                    isSeeking = false
                },
                valueRange = 0f..maxRange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playerState.formattedCurrentTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = VoxTheme.colors.subtleText
                )
                Text(
                    text = playerState.formattedTotalDuration,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = VoxTheme.colors.subtleText
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Main Playback Controls Row (5 evenly-spaced pure monochrome outline icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Shuffle
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Lucide.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.isShuffleEnabled) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 2. Previous Track (<=3s previous song, >3s restart song)
            IconButton(
                onClick = { viewModel.skipPrevious() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Lucide.SkipBack,
                    contentDescription = "Previous Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 3. Play / Pause (Larger ~34dp icon, clean monochrome)
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Lucide.Pause else Lucide.Play,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(34.dp)
                )
            }

            // 4. Next Track (Always next song)
            IconButton(
                onClick = { viewModel.skipNext() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Lucide.SkipForward,
                    contentDescription = "Next Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 5. Loop Mode
            IconButton(
                onClick = { viewModel.toggleLoopMode() },
                modifier = Modifier.size(44.dp)
            ) {
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

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Current Playing Queue Bottom Sheet
    if (showQueueSheet) {
        QueueBottomSheet(
            queue = currentQueue,
            currentTrackId = track.id,
            allAvailableTracks = allTracks,
            onTrackSelected = { index ->
                viewModel.skipToQueueIndex(index)
            },
            onRemoveFromQueue = { index ->
                viewModel.removeFromQueue(index)
            },
            onMoveTrack = { from, to ->
                viewModel.moveQueueItem(from, to)
            },
            onAddTrackToQueue = { t ->
                viewModel.addToQueue(t)
            },
            onClearQueue = {
                viewModel.clearQueueKeepCurrent()
            },
            onDismiss = { showQueueSheet = false }
        )
    }

    // Audio Output & Effects Bottom Sheet
    if (showAudioOutputSheet) {
        AudioOutputBottomSheet(
            routeState = audioRouteState,
            onRouteToSpeaker = { viewModel.routeToSpeaker() },
            onRouteToBluetooth = { viewModel.routeToBluetooth() },
            onToggleDolbyAtmos = { viewModel.toggleDolbyAtmos(it) },
            onDismiss = { showAudioOutputSheet = false }
        )
    }

    // Add to Saved Playlist Dialog
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onSelectPlaylist = { playlistId ->
                viewModel.addTrackToPlaylist(playlistId, track.id)
            },
            onCreatePlaylist = { playlistName ->
                viewModel.createPlaylist(playlistName, track.id)
            },
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    // Track Info & Audio Analysis Inspector Bottom Sheet
    if (showInfoSheet) {
        val audioMetadata = AudioMetadata(
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            bitrateKbps = track.bitrate,
            sampleRateHz = track.sampleRate,
            channels = 2,
            format = track.mimeType.substringAfterLast('/').uppercase(),
            fileSizeBytes = java.io.File(track.filePath).length().coerceAtLeast(1024L),
            filePath = track.filePath,
            bpm = track.bpm,
            musicalKey = track.musicalKey
        )
        AudioInspectorBottomSheet(
            metadata = audioMetadata,
            onDismiss = { showInfoSheet = false }
        )
    }

    // Settings & Audio Tools Bottom Sheet
    if (showSettingsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "DSP & AUDIO CONTROLS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Option: Graphic Equalizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showSettingsSheet = false
                            showEqualizerScreen = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Sliders,
                        contentDescription = "Graphic Equalizer",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Graphic Equalizer",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (equalizerState.isEnabled) "Status: Enabled (${equalizerState.currentPresetName})" else "Status: Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }

                // Option: Speed & Pitch DSP & A-B Looper & Sleep Timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showSettingsSheet = false
                            showSpeedPitchSheet = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Tv,
                        contentDescription = "Speed & Pitch Shifter",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Speed & Pitch Shifter, Looper & Sleep Timer",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Speed: %.2fx • Pitch: %+d st • A-B Loop • Sleep Timer".format(playerState.playbackSpeed, playerState.pitchSemitones),
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showSpeedPitchSheet) {
        SpeedPitchSheet(
            speed = playerState.playbackSpeed,
            pitchSemitones = playerState.pitchSemitones,
            pointA = playerState.pointA,
            pointB = playerState.pointB,
            sleepTimerRemainingMs = sleepTimerRemainingMs,
            isSleepTimerEndOfTrack = isSleepTimerEndOfTrack,
            onSpeedChange = { viewModel.setSpeed(it) },
            onPitchChange = { viewModel.setPitch(it) },
            onSetPointA = { viewModel.setPointA() },
            onSetPointB = { viewModel.setPointB() },
            onClearABLoop = { viewModel.clearABLoop() },
            onResetSpeed = { viewModel.resetSpeed() },
            onResetPitch = { viewModel.resetPitch() },
            onResetAll = { viewModel.resetAllDsp() },
            onStartSleepTimer = { viewModel.startSleepTimer(it) },
            onStartSleepTimerEndOfTrack = { viewModel.startSleepTimerEndOfTrack() },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { showSpeedPitchSheet = false }
        )
    }
}

