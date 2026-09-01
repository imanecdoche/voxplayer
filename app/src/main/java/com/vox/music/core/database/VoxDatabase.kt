package com.vox.music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vox.music.core.database.dao.AudioAnalysisDao
import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.database.dao.PlaylistDao
import com.vox.music.core.database.entity.AudioAnalysisCacheEntity
import com.vox.music.core.database.entity.AudioTrackEntity
import com.vox.music.core.database.entity.PlaylistEntity
import com.vox.music.core.database.entity.PlaylistTrackCrossRef

@Database(
    entities = [
        AudioTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        AudioAnalysisCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class VoxDatabase : RoomDatabase() {
    abstract fun audioTrackDao(): AudioTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun audioAnalysisDao(): AudioAnalysisDao

    companion object {
        const val DATABASE_NAME = "vox_music.db"
    }
}
