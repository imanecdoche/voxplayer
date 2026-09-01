package com.vox.music.core.lyrics.model

data class LyricsData(
    val lines: List<LyricLine> = emptyList(),
    val isSynced: Boolean = false,
    val plainText: String = "",
    val hasLyrics: Boolean = lines.isNotEmpty() || plainText.isNotBlank()
)
