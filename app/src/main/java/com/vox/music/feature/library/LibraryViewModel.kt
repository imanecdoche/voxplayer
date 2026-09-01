package com.vox.music.feature.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vox.music.core.audio.controller.MusicPlayerController
import com.vox.music.core.model.AudioMetadata
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.DirectoryGroup
import com.vox.music.core.model.Playlist
import com.vox.music.core.storage.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val playerController: MusicPlayerController,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("vox_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    val playerState = playerController.playerState

    init {
        val savedSort = prefs.getString("track_sort_order", TrackSortOrder.TITLE_ASC.name)
        val initialSort = try {
            TrackSortOrder.valueOf(savedSort ?: TrackSortOrder.TITLE_ASC.name)
        } catch (e: Exception) {
            TrackSortOrder.TITLE_ASC
        }
        _uiState.update { it.copy(sortOrder = initialSort) }
        observeDatabase()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            audioRepository.getAllTracks().collect { allTracks ->
                _uiState.update { it.copy(tracks = allTracks) }
            }
        }

        viewModelScope.launch {
            audioRepository.getDirectoryGroups().collect { directoryGroups ->
                _uiState.update { it.copy(folders = directoryGroups) }
            }
        }

        viewModelScope.launch {
            audioRepository.getFavoriteTracks().collect { favorites ->
                _uiState.update { it.copy(favoriteTracks = favorites) }
            }
        }

        viewModelScope.launch {
            audioRepository.getAllPlaylists().collect { playlistList ->
                _uiState.update { it.copy(playlists = playlistList) }
            }
        }

        viewModelScope.launch {
            audioRepository.getRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }

        // Start observing media store changes
        audioRepository.startObservingChanges {
            // Triggered on MediaStore update
        }
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.SelectTab -> {
                _uiState.update {
                    it.copy(
                        selectedTab = intent.tab,
                        selectedFolder = null,
                        selectedPlaylist = null,
                        isSearchActive = false,
                        searchQuery = ""
                    )
                }
            }

            is LibraryIntent.SelectFolder -> {
                _uiState.update { it.copy(selectedFolder = intent.folder, selectedPlaylist = null) }
                if (intent.folder != null) {
                    loadFolderTracks(intent.folder.folderPath)
                }
            }

            is LibraryIntent.SelectPlaylist -> {
                _uiState.update { it.copy(selectedPlaylist = intent.playlist, selectedFolder = null) }
                if (intent.playlist != null) {
                    loadPlaylistTracks(intent.playlist.id)
                }
            }

            is LibraryIntent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
                if (intent.query.isNotBlank()) {
                    searchTracks(intent.query)
                } else {
                    _uiState.update { it.copy(searchResults = emptyList()) }
                }
            }

            is LibraryIntent.ToggleSearch -> {
                _uiState.update {
                    it.copy(
                        isSearchActive = !it.isSearchActive,
                        searchQuery = if (it.isSearchActive) "" else it.searchQuery,
                        searchResults = if (it.isSearchActive) emptyList() else it.searchResults
                    )
                }
            }

            is LibraryIntent.ToggleFavorite -> {
                viewModelScope.launch {
                    audioRepository.toggleFavorite(intent.trackId, intent.isFavorite)
                }
            }

            is LibraryIntent.ScanStorage -> {
                scanStorage()
            }

            is LibraryIntent.SetPermissionGranted -> {
                _uiState.update { it.copy(hasStoragePermission = intent.isGranted) }
                if (intent.isGranted) {
                    scanStorage()
                }
            }

            // Dialog & Action Sheet Triggers
            is LibraryIntent.OpenTrackActions -> {
                _uiState.update { it.copy(activeActionTrack = intent.track) }
            }

            is LibraryIntent.AddTrackToQueue -> {
                playerController.addToQueue(intent.track)
            }

            is LibraryIntent.OpenTagEditor -> {
                _uiState.update { it.copy(trackForTagEditor = intent.track) }
            }

            is LibraryIntent.OpenRenameDialog -> {
                _uiState.update { it.copy(trackForRename = intent.track) }
            }

            is LibraryIntent.OpenAddToPlaylistDialog -> {
                _uiState.update { it.copy(trackForAddToPlaylist = intent.track) }
            }

            is LibraryIntent.OpenTrackDetails -> {
                _uiState.update { it.copy(trackForDetails = intent.track) }
            }

            // Step 7: ID3 Tag Editor & Inspector
            is LibraryIntent.OpenMetadataEditor -> {
                if (intent.track == null) {
                    _uiState.update { it.copy(trackForMetadataEditor = null) }
                } else {
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true) }
                        val result = audioRepository.readMetadata(intent.track.filePath)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                trackForMetadataEditor = result.getOrNull()
                            )
                        }
                    }
                }
            }

            is LibraryIntent.SaveMetadata -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    try {
                        audioRepository.writeMetadata(
                            intent.metadata.filePath,
                            intent.metadata,
                            intent.artworkBytes
                        )
                        audioRepository.syncMediaStore()
                        _uiState.update { it.copy(trackForMetadataEditor = null) }
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }

            is LibraryIntent.OpenInspector -> {
                if (intent.track == null) {
                    _uiState.update { it.copy(trackForInspector = null) }
                } else {
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true) }
                        val result = audioRepository.readMetadata(intent.track.filePath)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                trackForInspector = result.getOrNull()
                            )
                        }
                    }
                }
            }

            // Step 8: Audio Clipper & Trimmer
            is LibraryIntent.OpenClipper -> {
                if (intent.track == null) {
                    _uiState.update { it.copy(trackForClipper = null, clipperWaveform = FloatArray(0)) }
                } else {
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true, trackForClipper = intent.track) }
                        val waveform = audioRepository.getWaveform(intent.track.filePath)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                clipperWaveform = waveform
                            )
                        }
                    }
                }
            }

            is LibraryIntent.ExportTrimmedClip -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    try {
                        audioRepository.trimAudioLossless(
                            intent.track,
                            intent.startMs,
                            intent.endMs,
                            intent.customName
                        )
                        audioRepository.syncMediaStore()
                        _uiState.update { it.copy(trackForClipper = null) }
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }

            // Operations
            is LibraryIntent.SaveCustomTags -> {
                viewModelScope.launch {
                    audioRepository.updateCustomTags(intent.trackId, intent.tags)
                }
            }

            is LibraryIntent.RenameTrackFile -> {
                viewModelScope.launch {
                    audioRepository.renameAudioFile(intent.track, intent.newName)
                }
            }

            is LibraryIntent.DeleteTrack -> {
                viewModelScope.launch {
                    audioRepository.deleteAudioFileDirect(intent.track)
                }
            }

            is LibraryIntent.CreatePlaylist -> {
                viewModelScope.launch {
                    audioRepository.createPlaylist(intent.name)
                }
            }

            is LibraryIntent.DeletePlaylist -> {
                viewModelScope.launch {
                    audioRepository.deletePlaylist(intent.playlistId)
                    if (_uiState.value.selectedPlaylist?.id == intent.playlistId) {
                        _uiState.update { it.copy(selectedPlaylist = null) }
                    }
                }
            }

            is LibraryIntent.AddTrackToPlaylist -> {
                viewModelScope.launch {
                    audioRepository.addTrackToPlaylist(intent.playlistId, intent.trackId)
                }
            }

            is LibraryIntent.RemoveTrackFromPlaylist -> {
                viewModelScope.launch {
                    audioRepository.removeTrackFromPlaylist(intent.playlistId, intent.trackId)
                }
            }

            // Sorting & Search History
            is LibraryIntent.SetSortOrder -> {
                prefs.edit().putString("track_sort_order", intent.order.name).apply()
                _uiState.update { it.copy(sortOrder = intent.order, showSortBottomSheet = false) }
            }

            is LibraryIntent.SetShowSortBottomSheet -> {
                _uiState.update { it.copy(showSortBottomSheet = intent.show) }
            }

            is LibraryIntent.SetSearchViewOpen -> {
                _uiState.update {
                    it.copy(
                        isSearchViewOpen = intent.open,
                        searchQuery = if (!intent.open) "" else it.searchQuery,
                        searchResults = if (!intent.open) emptyList() else it.searchResults
                    )
                }
            }

            is LibraryIntent.AddSearchHistory -> {
                viewModelScope.launch {
                    audioRepository.addSearchQuery(intent.query)
                }
            }

            is LibraryIntent.DeleteSearchHistory -> {
                viewModelScope.launch {
                    audioRepository.deleteSearchQuery(intent.id)
                }
            }

            is LibraryIntent.ClearSearchHistory -> {
                viewModelScope.launch {
                    audioRepository.clearSearchHistory()
                }
            }
        }
    }

    private fun loadFolderTracks(folderPath: String) {
        viewModelScope.launch {
            audioRepository.getTracksByFolder(folderPath).collect { tracks ->
                _uiState.update { it.copy(folderTracks = tracks) }
            }
        }
    }

    private fun loadPlaylistTracks(playlistId: Long) {
        viewModelScope.launch {
            audioRepository.getTracksForPlaylist(playlistId).collect { tracks ->
                _uiState.update { it.copy(playlistTracks = tracks) }
            }
        }
    }

    private fun searchTracks(query: String) {
        viewModelScope.launch {
            audioRepository.searchTracks(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun scanStorage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                audioRepository.syncMediaStore()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRepository.stopObservingChanges()
    }
}
