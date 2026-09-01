package com.vox.music.core.storage

import android.content.Context
import com.vox.music.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uPlaylistManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun exportToM3u(playlistName: String, tracks: List<AudioTrack>, destinationFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sb = StringBuilder()
            sb.append("#EXTM3U\n")
            sb.append("#PLAYLIST:$playlistName\n\n")

            for (track in tracks) {
                val durationSec = track.durationMs / 1000
                sb.append("#EXTINF:$durationSec,${track.artist} - ${track.title}\n")
                sb.append("${track.filePath}\n")
            }

            destinationFile.writeText(sb.toString(), Charsets.UTF_8)
            Result.success(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun parseM3uFile(file: File): List<String> = withContext(Dispatchers.IO) {
        val filePaths = mutableListOf<String>()
        try {
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    filePaths.add(trimmed)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        filePaths
    }
}
