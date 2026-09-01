package com.vox.music.core.storage

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.vox.music.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    private val supportedExtensions = setOf("mp3", "wav", "flac", "m4a", "aac", "ogg", "opus")

    suspend fun scanStorageAudio(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioTrack>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        // Filter out non-music and very short sound clips (under 3 seconds)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 3000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        try {
            contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val filePath = cursor.getString(dataCol) ?: continue
                    val file = File(filePath)
                    val extension = file.extension.lowercase()

                    // Verify supported audio extension
                    if (extension !in supportedExtensions) {
                        continue
                    }

                    val id = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol) ?: file.nameWithoutExtension
                    val rawArtist = cursor.getString(artistCol)
                    val rawAlbum = cursor.getString(albumCol)
                    val durationMs = cursor.getLong(durationCol)
                    val mimeType = cursor.getString(mimeTypeCol) ?: "audio/$extension"
                    val dateAdded = cursor.getLong(dateAddedCol)
                    val dateModified = cursor.getLong(dateModifiedCol)

                    val parentFolder = file.parent ?: "/"
                    val fileName = file.name

                    val artist = if (rawArtist == null || rawArtist == "<unknown>") "Unknown Artist" else rawArtist
                    val album = if (rawAlbum == null || rawAlbum == "<unknown>") "Unknown Album" else rawAlbum
                    val title = if (rawTitle.isBlank()) file.nameWithoutExtension else rawTitle

                    tracks.add(
                        AudioTrack(
                            id = id,
                            filePath = filePath,
                            folderPath = parentFolder,
                            fileName = fileName,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = durationMs,
                            mimeType = mimeType,
                            bitrate = 0,
                            sampleRate = 44100,
                            dateAdded = dateAdded,
                            dateModified = dateModified
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tracks
    }
}
