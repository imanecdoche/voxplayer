package com.vox.music.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vox.music.core.model.AudioTrack

@Entity(
    tableName = "audio_tracks",
    indices = [
        Index(value = ["folderPath"]),
        Index(value = ["isFavorite"]),
        Index(value = ["title"]),
        Index(value = ["artist"])
    ]
)
data class AudioTrackEntity(
    @PrimaryKey
    val id: Long, // MediaStore Audio ID
    val filePath: String,
    val folderPath: String,
    val fileName: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mimeType: String,
    val bitrate: Int,
    val sampleRate: Int,
    val genre: String? = null,
    val year: Int? = null,
    val hasEmbeddedLyrics: Boolean = false,
    val bpm: Float? = null,
    val musicalKey: String? = null,
    val isFavorite: Boolean = false,
    val customTags: String = "", // Comma-separated or JSON list
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L
) {
    fun toDomainModel(): AudioTrack {
        return AudioTrack(
            id = id,
            filePath = filePath,
            folderPath = folderPath,
            fileName = fileName,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            mimeType = mimeType,
            bitrate = bitrate,
            sampleRate = sampleRate,
            genre = genre,
            year = year,
            hasEmbeddedLyrics = hasEmbeddedLyrics,
            bpm = bpm,
            musicalKey = musicalKey,
            isFavorite = isFavorite,
            customTags = if (customTags.isBlank()) emptyList() else customTags.split(",").map { it.trim() },
            dateAdded = dateAdded,
            dateModified = dateModified
        )
    }

    companion object {
        fun fromDomainModel(track: AudioTrack): AudioTrackEntity {
            return AudioTrackEntity(
                id = track.id,
                filePath = track.filePath,
                folderPath = track.folderPath,
                fileName = track.fileName,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                mimeType = track.mimeType,
                bitrate = track.bitrate,
                sampleRate = track.sampleRate,
                genre = track.genre,
                year = track.year,
                hasEmbeddedLyrics = track.hasEmbeddedLyrics,
                bpm = track.bpm,
                musicalKey = track.musicalKey,
                isFavorite = track.isFavorite,
                customTags = track.customTags.joinToString(","),
                dateAdded = track.dateAdded,
                dateModified = track.dateModified
            )
        }
    }
}
