package com.vox.music.core.model

/**
 * Domain model representing a physical storage folder containing audio files.
 */
data class DirectoryGroup(
    val folderPath: String,
    val folderName: String,
    val trackCount: Int,
    val totalDurationMs: Long
) {
    val totalDurationFormatted: String
        get() {
            val totalSeconds = totalDurationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%dh %dm".format(hours, minutes)
            } else {
                "%dm %ds".format(minutes, seconds)
            }
        }
}
