package com.vox.music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vox.music.core.database.entity.AudioTrackEntity
import com.vox.music.core.database.entity.FolderSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<AudioTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: AudioTrackEntity)

    @Update
    suspend fun update(track: AudioTrackEntity)

    @Query("SELECT * FROM audio_tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE folderPath = :folderPath ORDER BY title ASC")
    fun getTracksByFolder(folderPath: String): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTracks(): Flow<List<AudioTrackEntity>>

    @Query("""
        SELECT * FROM audio_tracks 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%' 
           OR fileName LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchTracks(query: String): Flow<List<AudioTrackEntity>>

    @Query("""
        SELECT 
            folderPath, 
            COUNT(id) as trackCount, 
            SUM(durationMs) as totalDurationMs 
        FROM audio_tracks 
        GROUP BY folderPath 
        ORDER BY folderPath ASC
    """)
    fun getFolderSummaries(): Flow<List<FolderSummary>>

    @Query("SELECT * FROM audio_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): AudioTrackEntity?

    @Query("UPDATE audio_tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun updateFavoriteStatus(trackId: Long, isFavorite: Boolean)

    @Query("UPDATE audio_tracks SET customTags = :customTags WHERE id = :trackId")
    suspend fun updateCustomTags(trackId: Long, customTags: String)

    @Query("UPDATE audio_tracks SET filePath = :newFilePath, fileName = :newFileName, title = :newTitle WHERE id = :trackId")
    suspend fun updateFilePathAndName(trackId: Long, newFilePath: String, newFileName: String, newTitle: String)

    @Query("UPDATE audio_tracks SET bpm = :bpm, musicalKey = :musicalKey WHERE id = :trackId")
    suspend fun updateBpmAndKey(trackId: Long, bpm: Double, musicalKey: String)

    @Query("DELETE FROM audio_tracks WHERE id = :trackId")
    suspend fun deleteById(trackId: Long)

    @Query("DELETE FROM audio_tracks WHERE id NOT IN (:validIds)")
    suspend fun deleteMissingTracks(validIds: List<Long>)

    @Query("DELETE FROM audio_tracks")
    suspend fun clearAll()
}
