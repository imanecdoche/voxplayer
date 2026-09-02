package com.vox.music.core.audio.service

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.google.common.collect.ImmutableList
import com.vox.music.R

@OptIn(UnstableApi::class)
class VoxMediaNotificationProvider(
    private val context: Context
) : DefaultMediaNotificationProvider(context) {

    init {
        setSmallIcon(R.drawable.ic_vox_logo)
    }

    override fun getMediaButtons(
        session: MediaSession,
        playerCommands: Player.Commands,
        customLayout: ImmutableList<CommandButton>,
        showWhenCompact: Boolean
    ): ImmutableList<CommandButton> {
        val player = session.player
        val buttons = ImmutableList.builder<CommandButton>()

        // 1. Shuffle Button
        val shuffleIcon = R.drawable.ic_widget_shuffle
        val shuffleButton = CommandButton.Builder()
            .setDisplayName(if (player.shuffleModeEnabled) "Shuffle On" else "Shuffle Off")
            .setIconResId(shuffleIcon)
            .setSessionCommand(SessionCommand(MusicPlaybackService.ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
            .setEnabled(true)
            .build()
        buttons.add(shuffleButton)

        // 2. Previous Button
        val prevButton = CommandButton.Builder()
            .setDisplayName("Previous")
            .setIconResId(R.drawable.ic_widget_prev)
            .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .setEnabled(true)
            .build()
        buttons.add(prevButton)

        // 3. Play / Pause Button
        val isPlaying = player.isPlaying
        val playPauseButton = CommandButton.Builder()
            .setDisplayName(if (isPlaying) "Pause" else "Play")
            .setIconResId(if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play)
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .setEnabled(true)
            .build()
        buttons.add(playPauseButton)

        // 4. Next Button
        val nextButton = CommandButton.Builder()
            .setDisplayName("Next")
            .setIconResId(R.drawable.ic_widget_next)
            .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .setEnabled(true)
            .build()
        buttons.add(nextButton)

        // 5. Repeat Button
        val repeatIcon = if (player.repeatMode == Player.REPEAT_MODE_ONE) {
            R.drawable.ic_widget_repeat_1
        } else {
            R.drawable.ic_widget_repeat
        }
        val repeatTitle = when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> "Repeat One"
            Player.REPEAT_MODE_ALL -> "Repeat All"
            else -> "Repeat Off"
        }
        val repeatButton = CommandButton.Builder()
            .setDisplayName(repeatTitle)
            .setIconResId(repeatIcon)
            .setSessionCommand(SessionCommand(MusicPlaybackService.ACTION_TOGGLE_REPEAT, Bundle.EMPTY))
            .setEnabled(true)
            .build()
        buttons.add(repeatButton)

        return buttons.build()
    }

    override fun addNotificationActions(
        session: MediaSession,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {
        super.addNotificationActions(session, mediaButtons, builder, actionFactory)
        // Ensure compact view shows Previous (1), Play/Pause (2), Next (3)
        return intArrayOf(1, 2, 3)
    }
}
