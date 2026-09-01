package com.vox.music.feature.library

import com.vox.music.core.database.entity.SearchHistoryEntity
import com.vox.music.core.model.AudioMetadata
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.DirectoryGroup
import com.vox.music.core.model.Playlist

enum class LibraryTab {
    FOLDERS,
    ALL_TRACKS,
    PLAYLISTS,
    FAVORITES
}

enum class TrackSortOrder(val displayName: String) {
    TITLE_ASC("A to Z (Title)"),
    TITLE_DESC("Z to A (Title)"),
    DATE_ADDED_DESC("Date Added (Newest)"),
    DATE_ADDED_ASC("Date Added (Oldest)"),
    DURATION_DESC("Length (Longest)"),
    DURATION_ASC("Length (Shortest)")
}

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.FOLDERS,
    val selectedFolder: DirectoryGroup? = null,
    val selectedPlaylist: Playlist? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isSearchViewOpen: Boolean = false,
    val sortOrder: TrackSortOrder = TrackSortOrder.TITLE_ASC,
    val showSortBottomSheet: Boolean = false,
    val folders: List<DirectoryGroup> = emptyList(),
    val tracks: List<AudioTrack> = emptyList(),
    val folderTracks: List<AudioTrack> = emptyList(),
    val favoriteTracks: List<AudioTrack> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val playlistTracks: List<AudioTrack> = emptyList(),
    val searchResults: List<AudioTrack> = emptyList(),
    val recentSearches: List<SearchHistoryEntity> = emptyList(),
    val activeActionTrack: AudioTrack? = null,
    val trackForTagEditor: AudioTrack? = null,
    val trackForRename: AudioTrack? = null,
    val trackForAddToPlaylist: AudioTrack? = null,
    val trackForDetails: AudioTrack? = null,
    val trackForMetadataEditor: AudioMetadata? = null,
    val trackForInspector: AudioMetadata? = null,
    val trackForClipper: AudioTrack? = null,
    val clipperWaveform: FloatArray = FloatArray(0),
    val isLoading: Boolean = false,
    val hasStoragePermission: Boolean = false
) {
    val displayedTracks: List<AudioTrack>
        get() {
            val baseList = when {
                isSearchActive && searchQuery.isNotBlank() -> searchResults
                selectedFolder != null -> folderTracks
                selectedPlaylist != null -> playlistTracks
                selectedTab == LibraryTab.FAVORITES -> favoriteTracks
                else -> tracks
            }
            return when (sortOrder) {
                TrackSortOrder.TITLE_ASC -> baseList.sortedBy { it.title.lowercase() }
                TrackSortOrder.TITLE_DESC -> baseList.sortedByDescending { it.title.lowercase() }
                TrackSortOrder.DATE_ADDED_DESC -> baseList.sortedByDescending { it.dateAdded }
                TrackSortOrder.DATE_ADDED_ASC -> baseList.sortedBy { it.dateAdded }
                TrackSortOrder.DURATION_DESC -> baseList.sortedByDescending { it.durationMs }
                TrackSortOrder.DURATION_ASC -> baseList.sortedBy { it.durationMs }
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LibraryUiState

        if (selectedTab != other.selectedTab) return false
        if (selectedFolder != other.selectedFolder) return false
        if (selectedPlaylist != other.selectedPlaylist) return false
        if (searchQuery != other.searchQuery) return false
        if (isSearchActive != other.isSearchActive) return false
        if (folders != other.folders) return false
        if (tracks != other.tracks) return false
        if (folderTracks != other.folderTracks) return false
        if (favoriteTracks != other.favoriteTracks) return false
        if (playlists != other.playlists) return false
        if (playlistTracks != other.playlistTracks) return false
        if (searchResults != other.searchResults) return false
        if (activeActionTrack != other.activeActionTrack) return false
        if (trackForTagEditor != other.trackForTagEditor) return false
        if (trackForRename != other.trackForRename) return false
        if (trackForAddToPlaylist != other.trackForAddToPlaylist) return false
        if (trackForDetails != other.trackForDetails) return false
        if (trackForMetadataEditor != other.trackForMetadataEditor) return false
        if (trackForInspector != other.trackForInspector) return false
        if (trackForClipper != other.trackForClipper) return false
        if (!clipperWaveform.contentEquals(other.clipperWaveform)) return false
        if (isLoading != other.isLoading) return false
        if (hasStoragePermission != other.hasStoragePermission) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectedTab.hashCode()
        result = 31 * result + (selectedFolder?.hashCode() ?: 0)
        result = 31 * result + (selectedPlaylist?.hashCode() ?: 0)
        result = 31 * result + searchQuery.hashCode()
        result = 31 * result + isSearchActive.hashCode()
        result = 31 * result + folders.hashCode()
        result = 31 * result + tracks.hashCode()
        result = 31 * result + folderTracks.hashCode()
        result = 31 * result + favoriteTracks.hashCode()
        result = 31 * result + playlists.hashCode()
        result = 31 * result + playlistTracks.hashCode()
        result = 31 * result + searchResults.hashCode()
        result = 31 * result + (activeActionTrack?.hashCode() ?: 0)
        result = 31 * result + (trackForTagEditor?.hashCode() ?: 0)
        result = 31 * result + (trackForRename?.hashCode() ?: 0)
        result = 31 * result + (trackForAddToPlaylist?.hashCode() ?: 0)
        result = 31 * result + (trackForDetails?.hashCode() ?: 0)
        result = 31 * result + (trackForMetadataEditor?.hashCode() ?: 0)
        result = 31 * result + (trackForInspector?.hashCode() ?: 0)
        result = 31 * result + (trackForClipper?.hashCode() ?: 0)
        result = 31 * result + clipperWaveform.contentHashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + hasStoragePermission.hashCode()
        return result
    }
}
