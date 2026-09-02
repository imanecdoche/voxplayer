package com.vox.music.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class WidgetTrackItem(
    val title: String,
    val artist: String,
    val filePath: String,
    val index: Int
)

data class WidgetState(
    val title: String,
    val artist: String,
    val album: String,
    val filePath: String,
    val isPlaying: Boolean,
    val isShuffle: Boolean,
    val loopMode: String,
    val queue: List<WidgetTrackItem>
)

object VoxWidgetHelper {
    private const val PREFS_NAME = "vox_widget_prefs"
    private const val KEY_TITLE = "widget_title"
    private const val KEY_ARTIST = "widget_artist"
    private const val KEY_ALBUM = "widget_album"
    private const val KEY_FILE_PATH = "widget_file_path"
    private const val KEY_IS_PLAYING = "widget_is_playing"
    private const val KEY_IS_SHUFFLE = "widget_is_shuffle"
    private const val KEY_LOOP_MODE = "widget_loop_mode"
    private const val KEY_QUEUE_JSON = "widget_queue_json"

    private val scope = CoroutineScope(Dispatchers.IO)

    fun saveWidgetState(
        context: Context,
        title: String,
        artist: String,
        album: String,
        filePath: String,
        isPlaying: Boolean,
        isShuffle: Boolean,
        loopMode: String,
        queue: List<WidgetTrackItem>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        queue.take(5).forEach { item ->
            val obj = JSONObject().apply {
                put("title", item.title)
                put("artist", item.artist)
                put("filePath", item.filePath)
                put("index", item.index)
            }
            jsonArray.put(obj)
        }

        prefs.edit()
            .putString(KEY_TITLE, title)
            .putString(KEY_ARTIST, artist)
            .putString(KEY_ALBUM, album)
            .putString(KEY_FILE_PATH, filePath)
            .putBoolean(KEY_IS_PLAYING, isPlaying)
            .putBoolean(KEY_IS_SHUFFLE, isShuffle)
            .putString(KEY_LOOP_MODE, loopMode)
            .putString(KEY_QUEUE_JSON, jsonArray.toString())
            .apply()

        scope.launch {
            try {
                VoxWidget().updateAll(context)
            } catch (e: Exception) {
                // Ignore widget update if glance isn't bound yet
            }
        }
    }

    fun getWidgetState(context: Context): WidgetState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString(KEY_TITLE, "Vox Player") ?: "Vox Player"
        val artist = prefs.getString(KEY_ARTIST, "No track playing") ?: "No track playing"
        val album = prefs.getString(KEY_ALBUM, "") ?: ""
        val filePath = prefs.getString(KEY_FILE_PATH, "") ?: ""
        val isPlaying = prefs.getBoolean(KEY_IS_PLAYING, false)
        val isShuffle = prefs.getBoolean(KEY_IS_SHUFFLE, false)
        val loopMode = prefs.getString(KEY_LOOP_MODE, "NONE") ?: "NONE"
        val queueJson = prefs.getString(KEY_QUEUE_JSON, "[]") ?: "[]"

        val queueList = mutableListOf<WidgetTrackItem>()
        try {
            val jsonArray = JSONArray(queueJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                queueList.add(
                    WidgetTrackItem(
                        title = obj.optString("title", ""),
                        artist = obj.optString("artist", ""),
                        filePath = obj.optString("filePath", ""),
                        index = obj.optInt("index", i)
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }

        return WidgetState(
            title = title,
            artist = artist,
            album = album,
            filePath = filePath,
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            loopMode = loopMode,
            queue = queueList
        )
    }

    fun loadArtworkBitmap(filePath: String, sizeDp: Int = 120, alphaFloat: Float = 1.0f): Bitmap? {
        if (filePath.isEmpty()) return null
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val picture = retriever.embeddedPicture
            retriever.release()
            if (picture != null) {
                val orig = BitmapFactory.decodeByteArray(picture, 0, picture.size) ?: return null
                val scaled = Bitmap.createScaledBitmap(orig, sizeDp, sizeDp, true)
                if (alphaFloat < 0.99f) {
                    val transparentBitmap = Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(transparentBitmap)
                    val paint = Paint().apply {
                        alpha = (alphaFloat * 255).toInt().coerceIn(0, 255)
                    }
                    canvas.drawBitmap(scaled, 0f, 0f, paint)
                    transparentBitmap
                } else {
                    scaled
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
