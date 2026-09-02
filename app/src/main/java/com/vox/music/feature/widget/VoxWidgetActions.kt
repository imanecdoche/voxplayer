package com.vox.music.feature.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.vox.music.core.audio.service.MusicPlaybackService

val TrackIndexKey = ActionParameters.Key<Int>("track_index_key")

class TogglePlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_WIDGET_TOGGLE_PLAY
        }
        context.startService(intent)
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_WIDGET_SKIP_NEXT
        }
        context.startService(intent)
    }
}

class SkipPrevAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_WIDGET_SKIP_PREVIOUS
        }
        context.startService(intent)
    }
}

class ToggleShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_WIDGET_TOGGLE_SHUFFLE
        }
        context.startService(intent)
    }
}

class ToggleRepeatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_WIDGET_TOGGLE_REPEAT
        }
        context.startService(intent)
    }
}

class PlayQueueIndexAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val targetIndex = parameters[TrackIndexKey] ?: 0
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_WIDGET_PLAY_INDEX
            putExtra(MusicPlaybackService.EXTRA_WIDGET_INDEX, targetIndex)
        }
        context.startService(intent)
    }
}
