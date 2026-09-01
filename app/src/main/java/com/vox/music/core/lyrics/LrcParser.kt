package com.vox.music.core.lyrics

import com.vox.music.core.lyrics.model.LyricLine
import com.vox.music.core.lyrics.model.LyricsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcParser @Inject constructor() {

    // Matches standard LRC timestamps: [01:23.45] or [01:23.456] or [1:23.45]
    private val lrcPattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?\\]")

    fun parseLrcContent(content: String): LyricsData {
        if (content.isBlank()) {
            return LyricsData()
        }

        val lines = mutableListOf<LyricLine>()
        val rawLines = content.lines()

        for (line in rawLines) {
            val matcher = lrcPattern.matcher(line)
            val lineTimestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (matcher.find()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val millisRaw = matcher.group(3)

                val millis = when (millisRaw?.length) {
                    1 -> (millisRaw.toLongOrNull() ?: 0L) * 100
                    2 -> (millisRaw.toLongOrNull() ?: 0L) * 10
                    3 -> millisRaw.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val timestampMs = (minutes * 60 + seconds) * 1000 + millis
                lineTimestamps.add(timestampMs)
                lastMatchEnd = matcher.end()
            }

            if (lineTimestamps.isNotEmpty()) {
                val text = line.substring(lastMatchEnd).trim()
                for (ts in lineTimestamps) {
                    lines.add(LyricLine(timestampMs = ts, content = text))
                }
            }
        }

        return if (lines.isNotEmpty()) {
            val sortedLines = lines.sortedBy { it.timestampMs }
            LyricsData(
                lines = sortedLines,
                isSynced = true,
                plainText = content
            )
        } else {
            // Unsynced plain text lyrics fallback
            LyricsData(
                lines = emptyList(),
                isSynced = false,
                plainText = content
            )
        }
    }

    suspend fun loadLyricsForAudioFile(
        audioFilePath: String,
        embeddedLyrics: String? = null
    ): LyricsData = withContext(Dispatchers.IO) {
        val audioFile = File(audioFilePath)

        // 1. Check for external .lrc file in the same directory
        val lrcFile = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
        if (lrcFile.exists() && lrcFile.canRead()) {
            try {
                val lrcText = lrcFile.readText(Charsets.UTF_8)
                val parsed = parseLrcContent(lrcText)
                if (parsed.hasLyrics) {
                    return@withContext parsed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Check embedded lyrics from ID3 tags
        if (!embeddedLyrics.isNullOrBlank()) {
            val parsed = parseLrcContent(embeddedLyrics)
            if (parsed.hasLyrics) {
                return@withContext parsed
            }
        }

        LyricsData()
    }

    fun findActiveLyricIndex(lines: List<LyricLine>, currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        if (currentPositionMs < lines.first().timestampMs) return 0

        var low = 0
        var high = lines.size - 1
        var result = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].timestampMs <= currentPositionMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return result
    }
}
