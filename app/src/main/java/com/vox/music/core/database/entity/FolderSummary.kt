package com.vox.music.core.database.entity

import com.vox.music.core.model.DirectoryGroup

/**
 * Data projection for folder group aggregation query.
 */
data class FolderSummary(
    val folderPath: String,
    val trackCount: Int,
    val totalDurationMs: Long
) {
    fun toDomainModel(): DirectoryGroup {
        val folderName = folderPath.trimEnd('/').substringAfterLast('/')
        return DirectoryGroup(
            folderPath = folderPath,
            folderName = if (folderName.isBlank()) "Root Storage" else folderName,
            trackCount = trackCount,
            totalDurationMs = totalDurationMs
        )
    }
}
