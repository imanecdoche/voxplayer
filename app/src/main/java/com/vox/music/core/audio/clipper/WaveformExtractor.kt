package com.vox.music.core.audio.clipper

import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max

@Singleton
class WaveformExtractor @Inject constructor() {

    private val waveformCache = mutableMapOf<String, FloatArray>()

    suspend fun extractWaveform(
        filePath: String,
        sampleCount: Int = 120
    ): FloatArray = withContext(Dispatchers.IO) {
        waveformCache[filePath]?.let { return@withContext it }

        val file = File(filePath)
        if (!file.exists()) {
            return@withContext FloatArray(sampleCount) { 0.2f }
        }

        val peaks = FloatArray(sampleCount) { 0.1f }

        var extractor: MediaExtractor? = null
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)

            var audioTrackIndex = -1
            var durationUs = 0L

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    if (format.containsKey(MediaFormat.KEY_DURATION)) {
                        durationUs = format.getLong(MediaFormat.KEY_DURATION)
                    }
                    break
                }
            }

            if (audioTrackIndex >= 0 && durationUs > 0) {
                extractor.selectTrack(audioTrackIndex)
                val stepUs = durationUs / sampleCount
                val buffer = ByteBuffer.allocate(16 * 1024)

                for (bucket in 0 until sampleCount) {
                    val targetUs = bucket * stepUs
                    extractor.seekTo(targetUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                    var maxAmp = 0
                    val read = extractor.readSampleData(buffer, 0)
                    if (read > 0) {
                        buffer.rewind()
                        // Sample short PCM values or raw bytes
                        while (buffer.hasRemaining() && buffer.remaining() >= 2) {
                            val sample = buffer.short
                            val amp = abs(sample.toInt())
                            if (amp > maxAmp) {
                                maxAmp = amp
                            }
                        }
                    }

                    val normalized = (maxAmp.toFloat() / Short.MAX_VALUE).coerceIn(0.08f, 1.0f)
                    peaks[bucket] = normalized
                }
            } else {
                // Fallback pseudo waveform if header metadata lacks track format
                val hash = file.name.hashCode()
                for (i in 0 until sampleCount) {
                    val amp = 0.2f + 0.7f * abs(Math.sin((i + hash).toDouble() * 0.3).toFloat())
                    peaks[i] = amp
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val hash = file.name.hashCode()
            for (i in 0 until sampleCount) {
                peaks[i] = 0.2f + 0.6f * abs(Math.sin((i + hash).toDouble() * 0.25).toFloat())
            }
        } finally {
            extractor?.runCatching { release() }
        }

        waveformCache[filePath] = peaks
        peaks
    }
}
