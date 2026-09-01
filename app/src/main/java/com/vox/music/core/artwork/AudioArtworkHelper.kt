package com.vox.music.core.artwork

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.LruCache
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

/**
 * Domain data wrapper representing an audio file path to load embedded artwork.
 */
data class AudioArtwork(val filePath: String)

/**
 * High-performance helper and memory LRU cache for extracting embedded audio artwork.
 */
object AudioArtworkHelper {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceAtLeast(1024 * 16) // Min 16MB cache
    
    private val memoryCache = object : LruCache<String, ByteArray>(cacheSize) {
        override fun sizeOf(key: String, value: ByteArray): Int {
            return (value.size / 1024).coerceAtLeast(1)
        }
    }

    private val EMPTY_MARKER = ByteArray(0)

    suspend fun getArtworkBytes(filePath: String): ByteArray? = withContext(Dispatchers.IO) {
        if (filePath.isBlank()) return@withContext null

        synchronized(memoryCache) {
            val cached = memoryCache.get(filePath)
            if (cached != null) {
                return@withContext if (cached.isEmpty()) null else cached
            }
        }

        var picture: ByteArray? = null

        // 1. First priority: Android native MediaMetadataRetriever
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            picture = retriever.embeddedPicture
            retriever.release()
        } catch (e: Exception) {
            // Ignore failure, try fallback
        }

        // 2. Second priority: Jaudiotagger binary tag reader
        if (picture == null || picture.isEmpty()) {
            try {
                val file = File(filePath)
                if (file.exists() && file.canRead()) {
                    val audioFile = AudioFileIO.read(file)
                    val tag = audioFile.tag
                    val artwork = tag?.firstArtwork
                    picture = artwork?.binaryData
                }
            } catch (e: Exception) {
                // Ignore fallback failure
            }
        }

        synchronized(memoryCache) {
            if (picture != null && picture.isNotEmpty()) {
                memoryCache.put(filePath, picture)
            } else {
                memoryCache.put(filePath, EMPTY_MARKER)
            }
        }

        picture
    }

    fun clearCache() {
        synchronized(memoryCache) {
            memoryCache.evictAll()
        }
    }
}

/**
 * Custom Coil Fetcher for [AudioArtwork] instances.
 */
class AudioArtworkFetcher(
    private val data: AudioArtwork,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val bytes = AudioArtworkHelper.getArtworkBytes(data.filePath) ?: return null
        return SourceResult(
            source = ImageSource(Buffer().write(bytes), options.context),
            mimeType = "image/jpeg",
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<AudioArtwork> {
        override fun create(data: AudioArtwork, options: Options, imageLoader: ImageLoader): Fetcher {
            return AudioArtworkFetcher(data, options)
        }
    }
}

/**
 * Custom Coil Fetcher for direct String audio file paths.
 */
class AudioStringPathFetcher(
    private val filePath: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val bytes = AudioArtworkHelper.getArtworkBytes(filePath) ?: return null
        return SourceResult(
            source = ImageSource(Buffer().write(bytes), options.context),
            mimeType = "image/jpeg",
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<String> {
        private val supportedExtensions = setOf("mp3", "flac", "m4a", "wav", "ogg", "opus", "aac")

        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            val ext = data.substringAfterLast('.', "").lowercase()
            if (ext in supportedExtensions) {
                return AudioStringPathFetcher(data, options)
            }
            return null
        }
    }
}
