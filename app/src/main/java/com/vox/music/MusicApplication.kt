package com.vox.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.vox.music.core.artwork.AudioArtworkFetcher
import com.vox.music.core.artwork.AudioStringPathFetcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(AudioArtworkFetcher.Factory())
                add(AudioStringPathFetcher.Factory())
            }
            .respectCacheHeaders(false)
            .build()
    }

    companion object {
        const val PLAYBACK_NOTIFICATION_CHANNEL_ID = "vox_playback_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackChannel = NotificationChannel(
                PLAYBACK_NOTIFICATION_CHANNEL_ID,
                "Playback Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and information for active audio playback"
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(playbackChannel)
        }
    }
}
