package com.vox.music.core.storage

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.vox.music.core.database.dao.AudioTrackDao
import com.vox.music.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioTrackDao: AudioTrackDao
) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun renameAudioFile(track: AudioTrack, newNameWithoutExt: String): Result<AudioTrack> = withContext(Dispatchers.IO) {
        try {
            val oldFile = File(track.filePath)
            if (!oldFile.exists()) {
                return@withContext Result.failure(Exception("File not found on storage"))
            }

            val extension = oldFile.extension
            val newFileName = "$newNameWithoutExt.$extension"
            val parentFolder = oldFile.parentFile ?: return@withContext Result.failure(Exception("Invalid parent directory"))
            val newFile = File(parentFolder, newFileName)

            val success = oldFile.renameTo(newFile)
            if (!success) {
                return@withContext Result.failure(Exception("Failed to rename physical file on disk"))
            }

            val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.id)
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, newFileName)
                put(MediaStore.Audio.Media.TITLE, newNameWithoutExt)
                put(MediaStore.Audio.Media.DATA, newFile.absolutePath)
            }

            try {
                contentResolver.update(trackUri, values, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            audioTrackDao.updateFilePathAndName(
                trackId = track.id,
                newFilePath = newFile.absolutePath,
                newFileName = newFileName,
                newTitle = newNameWithoutExt
            )

            val updatedTrack = track.copy(
                filePath = newFile.absolutePath,
                fileName = newFileName,
                title = newNameWithoutExt
            )

            Result.success(updatedTrack)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getDeleteIntentSender(track: AudioTrack): IntentSender? {
        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent: PendingIntent = MediaStore.createDeleteRequest(contentResolver, listOf(trackUri))
            pendingIntent.intentSender
        } else {
            null
        }
    }

    suspend fun deleteAudioFileDirect(track: AudioTrack): Boolean = withContext(Dispatchers.IO) {
        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.id)
        try {
            contentResolver.delete(trackUri, null, null)
            val file = File(track.filePath)
            if (file.exists()) {
                file.delete()
            }
            audioTrackDao.deleteById(track.id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTrackFromDatabase(trackId: Long) = withContext(Dispatchers.IO) {
        audioTrackDao.deleteById(trackId)
    }

    suspend fun updateCustomTags(trackId: Long, tags: List<String>) = withContext(Dispatchers.IO) {
        val tagsString = tags.joinToString(",")
        audioTrackDao.updateCustomTags(trackId, tagsString)
    }
}
