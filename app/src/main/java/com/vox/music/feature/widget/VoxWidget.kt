package com.vox.music.feature.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.vox.music.MainActivity
import com.vox.music.R

class VoxWidget : GlanceAppWidget() {

    companion object {
        val COMPACT_SQUARE = DpSize(100.dp, 100.dp)
        val HORIZONTAL_BAR = DpSize(220.dp, 90.dp)
        val EXPANDED_LARGE = DpSize(220.dp, 190.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(COMPACT_SQUARE, HORIZONTAL_BAR, EXPANDED_LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = VoxWidgetHelper.getWidgetState(context)
        val artworkBitmap = VoxWidgetHelper.loadArtworkBitmap(state.filePath, 240, 1.0f)
        val artworkOverlayBitmap = VoxWidgetHelper.loadArtworkBitmap(state.filePath, 240, 0.25f)

        provideContent {
            val size = LocalSize.current
            when {
                size.width >= 200.dp && size.height >= 170.dp -> {
                    ExpandedLargeLayout(state, artworkBitmap)
                }
                size.width >= 190.dp -> {
                    HorizontalBarLayout(state, artworkBitmap)
                }
                else -> {
                    CompactSquareLayout(state, artworkOverlayBitmap)
                }
            }
        }
    }
}

class VoxWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VoxWidget()
}

@Composable
private fun CompactSquareLayout(
    state: WidgetState,
    overlayBitmap: android.graphics.Bitmap?
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(Color(0xFF141416)))
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        if (overlayBitmap != null) {
            Image(
                provider = ImageProvider(overlayBitmap),
                contentDescription = "Cover Art Background",
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize().cornerRadius(24.dp)
            )
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WidgetIconButton(
                iconRes = R.drawable.ic_widget_prev,
                contentDescription = "Previous",
                onClick = actionRunCallback<SkipPrevAction>(),
                iconSize = 22.dp
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            WidgetIconButton(
                iconRes = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                onClick = actionRunCallback<TogglePlayPauseAction>(),
                iconSize = 30.dp
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            WidgetIconButton(
                iconRes = R.drawable.ic_widget_next,
                contentDescription = "Next",
                onClick = actionRunCallback<SkipNextAction>(),
                iconSize = 22.dp
            )
        }
    }
}

@Composable
private fun HorizontalBarLayout(
    state: WidgetState,
    artworkBitmap: android.graphics.Bitmap?
) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(Color(0xFF161618)))
            .padding(12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 1:1 Squircle Artwork
        Box(
            modifier = GlanceModifier
                .size(68.dp)
                .cornerRadius(14.dp)
                .background(ColorProvider(Color(0xFF242426))),
            contentAlignment = Alignment.Center
        ) {
            if (artworkBitmap != null) {
                Image(
                    provider = ImageProvider(artworkBitmap),
                    contentDescription = state.title,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize().cornerRadius(14.dp)
                )
            } else {
                Image(
                    provider = ImageProvider(R.drawable.vox_main_logomark),
                    contentDescription = "Vox",
                    modifier = GlanceModifier.size(32.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Right: Metadata & 5-Button Controls
        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = state.artist,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF9E9E9E)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // 5 Playback Controls
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WidgetIconButton(
                    iconRes = R.drawable.ic_widget_shuffle,
                    contentDescription = "Shuffle",
                    onClick = actionRunCallback<ToggleShuffleAction>(),
                    iconSize = 16.dp,
                    tint = if (state.isShuffle) Color.White else Color(0xFF6E6E73)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetIconButton(
                    iconRes = R.drawable.ic_widget_prev,
                    contentDescription = "Previous",
                    onClick = actionRunCallback<SkipPrevAction>(),
                    iconSize = 20.dp
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetIconButton(
                    iconRes = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    onClick = actionRunCallback<TogglePlayPauseAction>(),
                    iconSize = 24.dp
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetIconButton(
                    iconRes = R.drawable.ic_widget_next,
                    contentDescription = "Next",
                    onClick = actionRunCallback<SkipNextAction>(),
                    iconSize = 20.dp
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                val repeatIcon = if (state.loopMode == "ONE") R.drawable.ic_widget_repeat_1 else R.drawable.ic_widget_repeat
                val repeatTint = if (state.loopMode != "NONE") Color.White else Color(0xFF6E6E73)
                WidgetIconButton(
                    iconRes = repeatIcon,
                    contentDescription = "Repeat",
                    onClick = actionRunCallback<ToggleRepeatAction>(),
                    iconSize = 16.dp,
                    tint = repeatTint
                )
            }
        }
    }
}

@Composable
private fun ExpandedLargeLayout(
    state: WidgetState,
    artworkBitmap: android.graphics.Bitmap?
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(Color(0xFF161618)))
            .padding(14.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        // 1. Top Section: Full Track Control
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(64.dp)
                    .cornerRadius(14.dp)
                    .background(ColorProvider(Color(0xFF242426))),
                contentAlignment = Alignment.Center
            ) {
                if (artworkBitmap != null) {
                    Image(
                        provider = ImageProvider(artworkBitmap),
                        contentDescription = state.title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize().cornerRadius(14.dp)
                    )
                } else {
                    Image(
                        provider = ImageProvider(R.drawable.vox_main_logomark),
                        contentDescription = "Vox",
                        modifier = GlanceModifier.size(30.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = state.title,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = state.artist,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF9E9E9E)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WidgetIconButton(
                        iconRes = R.drawable.ic_widget_shuffle,
                        contentDescription = "Shuffle",
                        onClick = actionRunCallback<ToggleShuffleAction>(),
                        iconSize = 16.dp,
                        tint = if (state.isShuffle) Color.White else Color(0xFF6E6E73)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetIconButton(
                        iconRes = R.drawable.ic_widget_prev,
                        contentDescription = "Previous",
                        onClick = actionRunCallback<SkipPrevAction>(),
                        iconSize = 20.dp
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetIconButton(
                        iconRes = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        onClick = actionRunCallback<TogglePlayPauseAction>(),
                        iconSize = 24.dp
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetIconButton(
                        iconRes = R.drawable.ic_widget_next,
                        contentDescription = "Next",
                        onClick = actionRunCallback<SkipNextAction>(),
                        iconSize = 20.dp
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    val repeatIcon = if (state.loopMode == "ONE") R.drawable.ic_widget_repeat_1 else R.drawable.ic_widget_repeat
                    val repeatTint = if (state.loopMode != "NONE") Color.White else Color(0xFF6E6E73)
                    WidgetIconButton(
                        iconRes = repeatIcon,
                        contentDescription = "Repeat",
                        onClick = actionRunCallback<ToggleRepeatAction>(),
                        iconSize = 16.dp,
                        tint = repeatTint
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Divider
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ColorProvider(Color(0xFF2C2C2E)))
        ) {}

        Spacer(modifier = GlanceModifier.height(8.dp))

        // 2. Bottom Section: Current Queue / Up Next Panel
        Text(
            text = "UP NEXT",
            style = TextStyle(
                color = ColorProvider(Color(0xFF8E8E93)),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        if (state.queue.isNotEmpty()) {
            state.queue.take(3).forEach { item ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(
                            actionRunCallback<PlayQueueIndexAction>(
                                actionParametersOf(TrackIndexKey to item.index)
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_play),
                        contentDescription = "Play Track",
                        modifier = GlanceModifier.size(12.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF8E8E93)))
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = item.title,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = item.artist,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF8E8E93)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                }
            }
        } else {
            Text(
                text = "Queue is empty",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF636366)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = GlanceModifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun WidgetIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: androidx.glance.action.Action,
    iconSize: androidx.compose.ui.unit.Dp,
    tint: Color = Color.White
) {
    Box(
        modifier = GlanceModifier
            .size(36.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = contentDescription,
            modifier = GlanceModifier.size(iconSize),
            colorFilter = ColorFilter.tint(ColorProvider(tint))
        )
    }
}
