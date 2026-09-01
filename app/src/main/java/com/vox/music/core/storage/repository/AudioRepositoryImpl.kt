package com.vox.music.core.storage.repository

import android.content.IntentSender
import com.vox.music.core.audio.clipper.AudioClipperEngine
import com.vox.music.core.audio.clipper.WaveformExtractor
import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.database.dao.PlaylistDao
import com.vox.music.core.database.dao.SearchHistoryDao
import com.vox.music.core.database.entity.AudioTrackEntity
import com.vox.music.core.database.entity.PlaylistEntity
import com.vox.music.core.database.entity.PlaylistTrackCrossRef
import com.vox.music.core.database.entity.SearchHistoryEntity
import com.vox.music.core.metadata.AudioTagManager
import com.vox.music.core.model.AudioMetadata
import com.vox.music.core.model.AudioTrack
import com.vox.music.core.model.DirectoryGroup
import com.vox.music.core.model.Playlist
import com.vox.music.core.storage.AudioScanner
import com.vox.music.core.storage.M3uPlaylistManager
import com.vox.music.core.storage.MediaStoreObserver
import com.vox.music.core.storage.StorageFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val audioScanner: AudioScanner,
    private val audioTrackDao: AudioTrackDao,
    private val playlistDao: PlaylistDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val mediaStoreObserver: MediaStoreObserver,
    private val storageFileManager: StorageFileManager,
    private val m3uPlaylistManager: M3uPlaylistManager,
    private val audioTagManager: AudioTagManager,
    private val waveformExtractor: WaveformExtractor,
    private val audioClipperEngine: AudioClipperEngine
) : AudioRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllTracks(): Flow<List<AudioTrack>> {
        return audioTrackDao.getAllTracks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTracksByFolder(folderPath: String): Flow<List<AudioTrack>> {
        return audioTrackDao.getTracksByFolder(folderPath).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFavoriteTracks(): Flow<List<AudioTrack>> {
        return audioTrackDao.getFavoriteTracks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDirectoryGroups(): Flow<List<DirectoryGroup>> {
        return audioTrackDao.getFolderSummaries().map { summaries ->
            summaries.map { it.toDomainModel() }
        }
    }

    override fun searchTracks(query: String): Flow<List<AudioTrack>> {
        return audioTrackDao.searchTracks(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun syncMediaStore() = withContext(Dispatchers.IO) {
        val scannedTracks = audioScanner.scanStorageAudio()
        if (scannedTracks.isNotEmpty()) {
            val entities = scannedTracks.map { track ->
                val existing = audioTrackDao.getTrackById(track.id)
                val isFavorite = existing?.isFavorite ?: false
                val customTags = existing?.customTags ?: ""
                val bpm = existing?.bpm
                val musicalKey = existing?.musicalKey

                AudioTrackEntity.fromDomainModel(track).copy(
                    isFavorite = isFavorite,
                    customTags = customTags,
                    bpm = bpm,
                    musicalKey = musicalKey
                )
            }

            audioTrackDao.insertAll(entities)

            // Cleanup deleted files
            val validIds = scannedTracks.map { it.id }
            audioTrackDao.deleteMissingTracks(validIds)
        } else {
            audioTrackDao.clearAll()
        }
    }

    override suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        audioTrackDao.updateFavoriteStatus(trackId, isFavorite)
    }

    override fun startObservingChanges(onChanged: suspend () -> Unit) {
        mediaStoreObserver.registerObserver {
            repositoryScope.launch {
                syncMediaStore()
                onChanged()
            }
        }
    }

    override fun stopObservingChanges() {
        mediaStoreObserver.unregisterObserver()
    }

    // ==================== PLAYLIST MANAGEMENT ====================

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { list ->
            list.map { entity ->
                val count = playlistDao.getTrackCountForPlaylist(entity.id)
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    trackCount = count,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override fun getTracksForPlaylist(playlistId: Long): Flow<List<AudioTrack>> {
        return playlistDao.getTracksForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.createPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun updatePlaylistName(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        val existing = playlistDao.getPlaylistById(playlistId)
        if (existing != null) {
            playlistDao.updatePlaylist(existing.copy(name = newName, updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        val currentCount = playlistDao.getTrackCountForPlaylist(playlistId)
        playlistDao.addTrackToPlaylist(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                orderIndex = currentCount
            )
        )
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun reorderPlaylistTracks(playlistId: Long, trackIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.reorderPlaylistTracks(playlistId, trackIds)
    }

    override suspend fun exportPlaylistM3u(playlistId: Long, destinationFile: File): Result<File> = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return@withContext Result.failure(Exception("Playlist not found"))
        val tracks = playlistDao.getTracksForPlaylist(playlistId).first().map { it.toDomainModel() }
        m3uPlaylistManager.exportToM3u(playlist.name, tracks, destinationFile)
    }

    // ==================== SCOPED STORAGE FILE MANAGEMENT ====================

    override suspend fun renameAudioFile(track: AudioTrack, newNameWithoutExt: String): Result<AudioTrack> {
        return storageFileManager.renameAudioFile(track, newNameWithoutExt)
    }

    override fun getDeleteIntentSender(track: AudioTrack): IntentSender? {
        return storageFileManager.getDeleteIntentSender(track)
    }

    override suspend fun deleteAudioFileDirect(track: AudioTrack): Boolean {
        return storageFileManager.deleteAudioFileDirect(track)
    }

    override suspend fun deleteTrackFromDatabase(trackId: Long) {
        storageFileManager.deleteTrackFromDatabase(trackId)
    }

    override suspend fun updateCustomTags(trackId: Long, tags: List<String>) {
        storageFileManager.updateCustomTags(trackId, tags)
    }

    // ==================== METADATA & ID3 TAGGING ====================

    override suspend fun readMetadata(filePath: String): Result<AudioMetadata> {
        return audioTagManager.readMetadata(filePath)
    }

    override suspend fun writeMetadata(
        filePath: String,
        metadata: AudioMetadata,
        artworkBytes: ByteArray?
    ): Result<AudioMetadata> {
        return audioTagManager.writeMetadata(filePath, metadata, artworkBytes)
    }

    // ==================== AUDIO CLIPPER & WAVEFORM ====================

    override suspend fun getWaveform(filePath: String): FloatArray {
        return waveformExtractor.extractWaveform(filePath)
    }

    override suspend fun trimAudioLossless(
        track: AudioTrack,
        startMs: Long,
        endMs: Long,
        customName: String?
    ): Result<File> {
        return audioClipperEngine.trimAudioLossless(track, startMs, endMs, customName)
    }

    // ==================== SEARCH HISTORY ====================

    override fun getRecentSearches(): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentSearches()
    }

    override suspend fun addSearchQuery(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(
                SearchHistoryEntity(query = query.trim(), timestamp = System.currentTimeMillis())
            )
        }
    }

    override suspend fun deleteSearchQuery(id: Long) = withContext(Dispatchers.IO) {
        searchHistoryDao.deleteSearch(id)
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        searchHistoryDao.clearSearchHistory()
    }
}
