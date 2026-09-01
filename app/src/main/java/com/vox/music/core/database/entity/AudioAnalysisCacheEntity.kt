package com.vox.music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_analysis_cache")
data class AudioAnalysisCacheEntity(
    @PrimaryKey val trackId: Long,
    val filePath: String,
    val bpm: Double?,
    val musicalKey: String?,
    val chordProgressionJson: String?,
    val lastAnalyzed: Long = System.currentTimeMillis()
)
