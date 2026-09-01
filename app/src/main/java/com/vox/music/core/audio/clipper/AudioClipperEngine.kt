package com.vox.music.core.audio.clipper

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.os.Environment
import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.database.entity.AudioTrackEntity
import com.vox.music.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

enum class ClipExportFormat {
    LOSSLESS_M4A,
    AAC_128K,
    AAC_192K,
    AAC_320K
}

@Singleton
class AudioClipperEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioTrackDao: AudioTrackDao
) {

    suspend fun trimAudioLossless(
        sourceTrack: AudioTrack,
        startMs: Long,
        endMs: Long,
        customOutputName: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        val sourceFile = File(sourceTrack.filePath)
        if (!sourceFile.exists()) {
            return@withContext Result.failure(Exception("Source audio file not found"))
        }

        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Clipped"
        )
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val baseName = customOutputName?.ifBlank { null } ?: "${sourceFile.nameWithoutExtension}_clip_${System.currentTimeMillis()}"
        val outputFile = File(outputDir, "$baseName.m4a")

        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(sourceFile.absolutePath)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex < 0 || audioFormat == null) {
                return@withContext Result.failure(Exception("No audio track found in file"))
            }

            extractor.selectTrack(audioTrackIndex)

            // Seek to start position (in microseconds)
            val startUs = startMs * 1000L
            val endUs = endMs * 1000L
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            // Init Muxer
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                1024 * 1024
            }

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            var presentationTimeOffsetUs = -1L

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0) {
                    break
                }

                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs > endUs) {
                    break
                }

                if (sampleTimeUs >= startUs) {
                    if (presentationTimeOffsetUs < 0) {
                        presentationTimeOffsetUs = sampleTimeUs
                    }

                    bufferInfo.presentationTimeUs = sampleTimeUs - presentationTimeOffsetUs
                    bufferInfo.flags = extractor.sampleFlags
                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                }

                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            muxer = null

            extractor.release()
            extractor = null

            // Scan media and insert to Room
            MediaScannerConnection.scanFile(
                context,
                arrayOf(outputFile.absolutePath),
                arrayOf("audio/mp4")
            ) { path, _ ->
                // Registered in MediaStore
            }

            val durationMs = endMs - startMs
            val newTrack = AudioTrackEntity(
                id = System.currentTimeMillis(),
                filePath = outputFile.absolutePath,
                folderPath = outputDir.absolutePath,
                fileName = outputFile.name,
                title = baseName,
                artist = sourceTrack.artist,
                album = sourceTrack.album,
                durationMs = durationMs,
                mimeType = "audio/mp4",
                bitrate = sourceTrack.bitrate,
                sampleRate = sourceTrack.sampleRate,
                isFavorite = false,
                customTags = "Clipped",
                dateAdded = System.currentTimeMillis() / 1000,
                dateModified = System.currentTimeMillis() / 1000
            )
            audioTrackDao.insertAll(listOf(newTrack))

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            muxer?.runCatching {
                stop()
                release()
            }
            extractor?.runCatching { release() }
            Result.failure(e)
        }
    }
}
