package com.vox.music.core.metadata

import android.content.Context
import android.media.MediaScannerConnection
import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.model.AudioMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioTagManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioTrackDao: AudioTrackDao
) {

    suspend fun readMetadata(filePath: String): Result<AudioMetadata> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: $filePath"))
            }

            val audioFile: AudioFile = AudioFileIO.read(file)
            val tag: Tag? = audioFile.tag
            val header = audioFile.audioHeader

            val artworkBytes = tag?.firstArtwork?.binaryData

            val metadata = AudioMetadata(
                filePath = filePath,
                title = tag?.getFirst(FieldKey.TITLE).orEmpty().ifEmpty { file.nameWithoutExtension },
                artist = tag?.getFirst(FieldKey.ARTIST).orEmpty().ifEmpty { "Unknown Artist" },
                album = tag?.getFirst(FieldKey.ALBUM).orEmpty().ifEmpty { "Unknown Album" },
                albumArtist = tag?.getFirst(FieldKey.ALBUM_ARTIST).orEmpty(),
                genre = tag?.getFirst(FieldKey.GENRE).orEmpty(),
                year = tag?.getFirst(FieldKey.YEAR).orEmpty(),
                trackNumber = tag?.getFirst(FieldKey.TRACK).orEmpty(),
                discNumber = tag?.getFirst(FieldKey.DISC_NO).orEmpty(),
                composer = tag?.getFirst(FieldKey.COMPOSER).orEmpty(),
                comment = tag?.getFirst(FieldKey.COMMENT).orEmpty(),
                lyrics = tag?.getFirst(FieldKey.LYRICS).orEmpty(),
                artworkBytes = artworkBytes,
                mimeType = "audio/" + file.extension.lowercase(),
                bitrateKbps = header?.bitRateAsNumber?.toInt() ?: 0,
                sampleRateHz = header?.sampleRateAsNumber ?: 0,
                channels = if (header != null && header.channels.isNotBlank()) {
                    header.channels.toIntOrNull() ?: 2
                } else 2,
                format = header?.format.orEmpty().ifEmpty { file.extension.uppercase() },
                fileSizeBytes = file.length(),
                durationMs = (header?.preciseTrackLength ?: 0.0).times(1000).toLong()
            )

            Result.success(metadata)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun writeMetadata(
        filePath: String,
        updated: AudioMetadata,
        newArtworkBytes: ByteArray? = null
    ): Result<AudioMetadata> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: $filePath"))
            }

            val audioFile: AudioFile = AudioFileIO.read(file)
            val tag: Tag = audioFile.tagOrCreateAndSetDefault

            // Set text fields
            setTagFieldSafely(tag, FieldKey.TITLE, updated.title)
            setTagFieldSafely(tag, FieldKey.ARTIST, updated.artist)
            setTagFieldSafely(tag, FieldKey.ALBUM, updated.album)
            setTagFieldSafely(tag, FieldKey.ALBUM_ARTIST, updated.albumArtist)
            setTagFieldSafely(tag, FieldKey.GENRE, updated.genre)
            setTagFieldSafely(tag, FieldKey.YEAR, updated.year)
            setTagFieldSafely(tag, FieldKey.TRACK, updated.trackNumber)
            setTagFieldSafely(tag, FieldKey.DISC_NO, updated.discNumber)
            setTagFieldSafely(tag, FieldKey.COMPOSER, updated.composer)
            setTagFieldSafely(tag, FieldKey.COMMENT, updated.comment)
            setTagFieldSafely(tag, FieldKey.LYRICS, updated.lyrics)

            // Artwork
            if (newArtworkBytes != null && newArtworkBytes.isNotEmpty()) {
                val tempArtworkFile = File.createTempFile("vox_art_", ".img", context.cacheDir)
                FileOutputStream(tempArtworkFile).use { it.write(newArtworkBytes) }

                try {
                    val artwork = ArtworkFactory.createArtworkFromFile(tempArtworkFile)
                    tag.deleteArtworkField()
                    tag.setField(artwork)
                } finally {
                    tempArtworkFile.delete()
                }
            }

            // Write to file
            AudioFileIO.write(audioFile)

            // Trigger system media scan
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null
            ) { _, _ -> }

            // Re-read updated metadata
            readMetadata(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun setTagFieldSafely(tag: Tag, key: FieldKey, value: String) {
        try {
            if (value.isNotBlank()) {
                tag.setField(key, value)
            } else {
                tag.deleteField(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
