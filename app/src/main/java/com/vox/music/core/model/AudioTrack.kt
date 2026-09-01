package com.vox.music.core.model

/**
 * Domain model representing an audio track in Vox.
 */
data class AudioTrack(
    val id: Long,
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
    val customTags: List<String> = emptyList(),
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}
