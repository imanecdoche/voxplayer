package com.vox.music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vox.music.core.database.entity.AudioAnalysisCacheEntity

@Dao
interface AudioAnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cache: AudioAnalysisCacheEntity)

    @Query("SELECT * FROM audio_analysis_cache WHERE trackId = :trackId LIMIT 1")
    suspend fun getCacheByTrackId(trackId: Long): AudioAnalysisCacheEntity?

    @Query("DELETE FROM audio_analysis_cache WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Long)

    @Query("DELETE FROM audio_analysis_cache")
    suspend fun clearAll()
}
