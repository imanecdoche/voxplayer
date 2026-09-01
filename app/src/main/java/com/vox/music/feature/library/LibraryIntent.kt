package com.vox.music.feature.library

import com.vox.music.core.model.AudioMetadata
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.DirectoryGroup
import com.vox.music.core.model.Playlist

sealed interface LibraryIntent {
    data class SelectTab(val tab: LibraryTab) : LibraryIntent
    data class SelectFolder(val folder: DirectoryGroup?) : LibraryIntent
    data class SelectPlaylist(val playlist: Playlist?) : LibraryIntent
    data class UpdateSearchQuery(val query: String) : LibraryIntent
    data object ToggleSearch : LibraryIntent
    data class ToggleFavorite(val trackId: Long, val isFavorite: Boolean) : LibraryIntent
    data object ScanStorage : LibraryIntent
    data class SetPermissionGranted(val isGranted: Boolean) : LibraryIntent

    // Action Sheet & Dialogs
    data class OpenTrackActions(val track: AudioTrack?) : LibraryIntent
    data class OpenTagEditor(val track: AudioTrack?) : LibraryIntent
    data class OpenRenameDialog(val track: AudioTrack?) : LibraryIntent
    data class OpenAddToPlaylistDialog(val track: AudioTrack?) : LibraryIntent
    data class OpenTrackDetails(val track: AudioTrack?) : LibraryIntent

    // Step 7: ID3 Tagging & Inspector
    data class OpenMetadataEditor(val track: AudioTrack?) : LibraryIntent
    data class SaveMetadata(val metadata: AudioMetadata, val artworkBytes: ByteArray?) : LibraryIntent
    data class OpenInspector(val track: AudioTrack?) : LibraryIntent

    // Step 8: Audio Clipper & Trimmer
    data class OpenClipper(val track: AudioTrack?) : LibraryIntent
    data class ExportTrimmedClip(val track: AudioTrack, val startMs: Long, val endMs: Long, val customName: String?) : LibraryIntent

    // File & Metadata Operations
    data class SaveCustomTags(val trackId: Long, val tags: List<String>) : LibraryIntent
    data class RenameTrackFile(val track: AudioTrack, val newName: String) : LibraryIntent
    data class DeleteTrack(val track: AudioTrack) : LibraryIntent

    // Playlist Operations
    data class CreatePlaylist(val name: String) : LibraryIntent
    data class DeletePlaylist(val playlistId: Long) : LibraryIntent
    data class AddTrackToPlaylist(val playlistId: Long, val trackId: Long) : LibraryIntent
    data class RemoveTrackFromPlaylist(val playlistId: Long, val trackId: Long) : LibraryIntent

    // Sorting & Search History Operations
    data class SetSortOrder(val order: TrackSortOrder) : LibraryIntent
    data class SetShowSortBottomSheet(val show: Boolean) : LibraryIntent
    data class SetSearchViewOpen(val open: Boolean) : LibraryIntent
    data class AddSearchHistory(val query: String) : LibraryIntent
    data class DeleteSearchHistory(val id: Long) : LibraryIntent
    data object ClearSearchHistory : LibraryIntent
}
