package com.vox.music.feature.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.RotateCw
import com.composables.icons.lucide.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vox.music.core.model.AudioTrack
import com.vox.music.feature.library.components.AddToPlaylistDialog
import com.vox.music.feature.library.components.FolderListItem
import com.vox.music.feature.library.components.PermissionRequestView
import com.vox.music.feature.library.components.PlaylistsView
import com.vox.music.feature.library.components.RenameFileDialog
import com.vox.music.feature.library.components.TagEditorDialog
import com.vox.music.feature.library.components.TrackActionsSheet
import com.vox.music.feature.library.components.TrackDetailsDialog
import com.vox.music.feature.library.components.TrackListItem
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxHeader
import com.vox.music.ui.theme.VoxTheme

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onTrackSelected: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onIntent(LibraryIntent.SetPermissionGranted(isGranted))
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permissionToRequest
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onIntent(LibraryIntent.SetPermissionGranted(isGranted))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        when {
            uiState.selectedFolder != null -> {
                // Folder detail header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.onIntent(LibraryIntent.SelectFolder(null)) }
                    ) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.selectedFolder!!.folderName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.folderTracks.size} tracks",
                            style = MaterialTheme.typography.labelSmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }
            }

            uiState.selectedPlaylist != null -> {
                // Playlist detail header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.onIntent(LibraryIntent.SelectPlaylist(null)) }
                    ) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.selectedPlaylist!!.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.playlistTracks.size} tracks",
                            style = MaterialTheme.typography.labelSmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }
            }

            else -> {
                VoxHeader(
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.onIntent(LibraryIntent.ToggleSearch) }
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSearchActive) Lucide.X else Lucide.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.onIntent(LibraryIntent.ScanStorage) }
                            ) {
                                Icon(
                                    imageVector = Lucide.RotateCw,
                                    contentDescription = "Scan Storage",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            }
        }

        HairlineDivider()

        if (!uiState.hasStoragePermission) {
            PermissionRequestView(
                onRequestPermission = { permissionLauncher.launch(permissionToRequest) }
            )
            return@Column
        }

        // Search Bar
        if (uiState.isSearchActive && uiState.selectedFolder == null && uiState.selectedPlaylist == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onIntent(LibraryIntent.UpdateSearchQuery(it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    decorationBox = { innerTextField ->
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                text = "Search tracks, artists, tags...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VoxTheme.colors.subtleText
                            )
                        }
                        innerTextField()
                    }
                )
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onIntent(LibraryIntent.UpdateSearchQuery("")) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = "Clear Search",
                            tint = VoxTheme.colors.subtleText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            HairlineDivider()
        }

        // TabRow Navigation (4 Tabs: DIRECTORIES, ALL TRACKS, PLAYLISTS, FAVORITES)
        if (uiState.selectedFolder == null && uiState.selectedPlaylist == null && !uiState.isSearchActive) {
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        height = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            ) {
                LibraryTab.entries.forEach { tab ->
                    val isSelected = uiState.selectedTab == tab
                    val tabTitle = when (tab) {
                        LibraryTab.FOLDERS -> "FOLDERS"
                        LibraryTab.ALL_TRACKS -> "TRACKS"
                        LibraryTab.PLAYLISTS -> "PLAYLISTS"
                        LibraryTab.FAVORITES -> "FAVORITES"
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.onIntent(LibraryIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = tabTitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText
                            )
                        }
                    )
                }
            }
            HairlineDivider()
        }

        // Content Area
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onBackground,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                uiState.selectedFolder != null -> {
                    TrackListView(
                        tracks = uiState.folderTracks,
                        emptyMessage = "No audio tracks in this folder",
                        onTrackSelected = onTrackSelected,
                        onToggleFavorite = { trackId, isFav ->
                            viewModel.onIntent(LibraryIntent.ToggleFavorite(trackId, isFav))
                        },
                        onMoreOptions = { track ->
                            viewModel.onIntent(LibraryIntent.OpenTrackActions(track))
                        }
                    )
                }

                uiState.selectedPlaylist != null -> {
                    TrackListView(
                        tracks = uiState.playlistTracks,
                        emptyMessage = "No tracks in this playlist.\nTap ':' on any track to add it.",
                        onTrackSelected = onTrackSelected,
                        onToggleFavorite = { trackId, isFav ->
                            viewModel.onIntent(LibraryIntent.ToggleFavorite(trackId, isFav))
                        },
                        onMoreOptions = { track ->
                            viewModel.onIntent(LibraryIntent.OpenTrackActions(track))
                        }
                    )
                }

                uiState.isSearchActive -> {
                    TrackListView(
                        tracks = uiState.searchResults,
                        emptyMessage = if (uiState.searchQuery.isEmpty()) "Type to search..." else "No matching tracks found",
                        onTrackSelected = onTrackSelected,
                        onToggleFavorite = { trackId, isFav ->
                            viewModel.onIntent(LibraryIntent.ToggleFavorite(trackId, isFav))
                        },
                        onMoreOptions = { track ->
                            viewModel.onIntent(LibraryIntent.OpenTrackActions(track))
                        }
                    )
                }

                uiState.selectedTab == LibraryTab.FOLDERS -> {
                    if (uiState.folders.isEmpty()) {
                        EmptyStateView(message = "No audio directories found.\nTap refresh to scan storage.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${uiState.folders.size} DIRECTORIES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VoxTheme.colors.subtleText
                                    )
                                    Text(
                                        text = "${uiState.tracks.size} TOTAL TRACKS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VoxTheme.colors.subtleText
                                    )
                                }
                                HairlineDivider()
                            }
                            items(uiState.folders, key = { it.folderPath }) { folder ->
                                FolderListItem(
                                    folder = folder,
                                    onClick = { viewModel.onIntent(LibraryIntent.SelectFolder(folder)) }
                                )
                            }
                        }
                    }
                }

                uiState.selectedTab == LibraryTab.ALL_TRACKS -> {
                    TrackListView(
                        tracks = uiState.tracks,
                        emptyMessage = "No audio tracks found.\nTap refresh to scan storage.",
                        onTrackSelected = onTrackSelected,
                        onToggleFavorite = { trackId, isFav ->
                            viewModel.onIntent(LibraryIntent.ToggleFavorite(trackId, isFav))
                        },
                        onMoreOptions = { track ->
                            viewModel.onIntent(LibraryIntent.OpenTrackActions(track))
                        }
                    )
                }

                uiState.selectedTab == LibraryTab.PLAYLISTS -> {
                    PlaylistsView(
                        playlists = uiState.playlists,
                        onSelectPlaylist = { playlist ->
                            viewModel.onIntent(LibraryIntent.SelectPlaylist(playlist))
                        },
                        onCreateNewPlaylist = { showCreatePlaylistDialog = true },
                        onDeletePlaylist = { playlistId ->
                            viewModel.onIntent(LibraryIntent.DeletePlaylist(playlistId))
                        }
                    )
                }

                uiState.selectedTab == LibraryTab.FAVORITES -> {
                    TrackListView(
                        tracks = uiState.favoriteTracks,
                        emptyMessage = "No favorite tracks yet.\nTap star on any track to add.",
                        onTrackSelected = onTrackSelected,
                        onToggleFavorite = { trackId, isFav ->
                            viewModel.onIntent(LibraryIntent.ToggleFavorite(trackId, isFav))
                        },
                        onMoreOptions = { track ->
                            viewModel.onIntent(LibraryIntent.OpenTrackActions(track))
                        }
                    )
                }
            }
        }
    }

    // ==================== DIALOGS & ACTIONS BOTTOM SHEETS ====================

    // Track Actions Sheet
    uiState.activeActionTrack?.let { track ->
        TrackActionsSheet(
            track = track,
            onToggleFavorite = { viewModel.onIntent(LibraryIntent.ToggleFavorite(track.id, !track.isFavorite)) },
            onAddToPlaylist = { viewModel.onIntent(LibraryIntent.OpenAddToPlaylistDialog(track)) },
            onEditTags = { viewModel.onIntent(LibraryIntent.OpenTagEditor(track)) },
            onEditMetadata = { viewModel.onIntent(LibraryIntent.OpenMetadataEditor(track)) },
            onClipAudio = { viewModel.onIntent(LibraryIntent.OpenClipper(track)) },
            onRenameFile = { viewModel.onIntent(LibraryIntent.OpenRenameDialog(track)) },
            onInspectAudio = { viewModel.onIntent(LibraryIntent.OpenInspector(track)) },
            onDeleteFile = { viewModel.onIntent(LibraryIntent.DeleteTrack(track)) },
            onDismiss = { viewModel.onIntent(LibraryIntent.OpenTrackActions(null)) }
        )
    }

    // Step 7: ID3 Tag & Metadata Editor Screen
    uiState.trackForMetadataEditor?.let { metadata ->
        com.vox.music.feature.metadata.MetadataEditorScreen(
            metadata = metadata,
            onSave = { updated, artworkBytes ->
                viewModel.onIntent(LibraryIntent.SaveMetadata(updated, artworkBytes))
            },
            onNavigateBack = { viewModel.onIntent(LibraryIntent.OpenMetadataEditor(null)) },
            isLoading = uiState.isLoading
        )
    }

    // Step 7: Audio Signal & Metadata Inspector BottomSheet
    uiState.trackForInspector?.let { metadata ->
        com.vox.music.feature.metadata.AudioInspectorBottomSheet(
            metadata = metadata,
            onDismiss = { viewModel.onIntent(LibraryIntent.OpenInspector(null)) }
        )
    }

    // Step 8: Audio Clipper & Trimmer Screen
    uiState.trackForClipper?.let { track ->
        com.vox.music.feature.clipper.AudioClipperScreen(
            track = track,
            waveformPeaks = uiState.clipperWaveform,
            currentPreviewPositionMs = 0L,
            isPreviewPlaying = false,
            onTogglePreview = { _, _ -> },
            onExportClip = { startMs, endMs, customName ->
                viewModel.onIntent(LibraryIntent.ExportTrimmedClip(track, startMs, endMs, customName))
            },
            onNavigateBack = { viewModel.onIntent(LibraryIntent.OpenClipper(null)) },
            isLoading = uiState.isLoading
        )
    }

    // Tag Editor Dialog
    uiState.trackForTagEditor?.let { track ->
        TagEditorDialog(
            initialTags = track.customTags,
            onSaveTags = { tags -> viewModel.onIntent(LibraryIntent.SaveCustomTags(track.id, tags)) },
            onDismiss = { viewModel.onIntent(LibraryIntent.OpenTagEditor(null)) }
        )
    }

    // Rename File Dialog
    uiState.trackForRename?.let { track ->
        RenameFileDialog(
            currentName = track.title,
            onConfirmRename = { newName -> viewModel.onIntent(LibraryIntent.RenameTrackFile(track, newName)) },
            onDismiss = { viewModel.onIntent(LibraryIntent.OpenRenameDialog(null)) }
        )
    }

    // Add to Playlist Dialog
    uiState.trackForAddToPlaylist?.let { track ->
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onSelectPlaylist = { playlistId -> viewModel.onIntent(LibraryIntent.AddTrackToPlaylist(playlistId, track.id)) },
            onCreatePlaylist = { name ->
                viewModel.onIntent(LibraryIntent.CreatePlaylist(name))
            },
            onDismiss = { viewModel.onIntent(LibraryIntent.OpenAddToPlaylistDialog(null)) }
        )
    }

    // Track Details Dialog
    uiState.trackForDetails?.let { track ->
        TrackDetailsDialog(
            track = track,
            onDismiss = { viewModel.onIntent(LibraryIntent.OpenTrackDetails(null)) }
        )
    }

    // Create New Playlist Dialog from tab
    if (showCreatePlaylistDialog) {
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onSelectPlaylist = {},
            onCreatePlaylist = { name ->
                viewModel.onIntent(LibraryIntent.CreatePlaylist(name))
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }
}

@Composable
private fun TrackListView(
    tracks: List<AudioTrack>,
    emptyMessage: String,
    onTrackSelected: (AudioTrack) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onMoreOptions: (AudioTrack) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyStateView(message = emptyMessage)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${tracks.size} TRACKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoxTheme.colors.subtleText
                    )
                }
                HairlineDivider()
            }
            items(tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    onClick = { onTrackSelected(track) },
                    onToggleFavorite = { isFav -> onToggleFavorite(track.id, isFav) },
                    onMoreOptions = { onMoreOptions(track) }
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = VoxTheme.colors.subtleText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
