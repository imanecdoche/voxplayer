package com.vox.music.core.storage.repository

import android.content.IntentSender
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.DirectoryGroup
import com.vox.music.core.model.Playlist
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRepository {
    fun getAllTracks(): Flow<List<AudioTrack>>
    fun getTracksByFolder(folderPath: String): Flow<List<AudioTrack>>
    fun getFavoriteTracks(): Flow<List<AudioTrack>>
    fun getDirectoryGroups(): Flow<List<DirectoryGroup>>
    fun searchTracks(query: String): Flow<List<AudioTrack>>
    suspend fun syncMediaStore()
    suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean)
    fun startObservingChanges(onChanged: suspend () -> Unit)
    fun stopObservingChanges()

    // Playlist Management
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getTracksForPlaylist(playlistId: Long): Flow<List<AudioTrack>>
    suspend fun createPlaylist(name: String): Long
    suspend fun updatePlaylistName(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun reorderPlaylistTracks(playlistId: Long, trackIds: List<Long>)
    suspend fun exportPlaylistM3u(playlistId: Long, destinationFile: File): Result<File>

    // Scoped Storage File Management
    suspend fun renameAudioFile(track: AudioTrack, newNameWithoutExt: String): Result<AudioTrack>
    fun getDeleteIntentSender(track: AudioTrack): IntentSender?
    suspend fun deleteAudioFileDirect(track: AudioTrack): Boolean
    suspend fun deleteTrackFromDatabase(trackId: Long)
    suspend fun updateCustomTags(trackId: Long, tags: List<String>)

    // Metadata & ID3 Tagging
    suspend fun readMetadata(filePath: String): Result<com.vox.music.core.model.AudioMetadata>
    suspend fun writeMetadata(filePath: String, metadata: com.vox.music.core.model.AudioMetadata, artworkBytes: ByteArray?): Result<com.vox.music.core.model.AudioMetadata>

    // Audio Clipper & Waveform
    suspend fun getWaveform(filePath: String): FloatArray
    suspend fun trimAudioLossless(track: AudioTrack, startMs: Long, endMs: Long, customName: String?): Result<File>

    // Search History
    fun getRecentSearches(): Flow<List<com.vox.music.core.database.entity.SearchHistoryEntity>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(id: Long)
    suspend fun clearSearchHistory()
}
