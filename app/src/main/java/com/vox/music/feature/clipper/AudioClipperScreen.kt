package com.vox.music.feature.clipper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Scissors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

@Composable
fun AudioClipperScreen(
    track: AudioTrack,
    waveformPeaks: FloatArray,
    currentPreviewPositionMs: Long,
    isPreviewPlaying: Boolean,
    onTogglePreview: (Long, Long) -> Unit,
    onExportClip: (Long, Long, String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = track.durationMs.coerceAtLeast(1000L)

    var startMs by remember { mutableLongStateOf(0L) }
    var endMs by remember { mutableLongStateOf(totalDurationMs) }
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "AUDIO CLIPPER & TRIMMER",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { showExportDialog = true },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Lucide.Scissors,
                    contentDescription = "Export Clip",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EXPORT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HairlineDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Track Info
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • Total Duration: ${formatTime(totalDurationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = VoxTheme.colors.subtleText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Waveform Canvas Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(totalDurationMs) {
                        detectTapGestures { offset ->
                            val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                            val tappedMs = (ratio * totalDurationMs).toLong()
                            // Set closest marker
                            if (kotlin.math.abs(tappedMs - startMs) < kotlin.math.abs(tappedMs - endMs)) {
                                startMs = tappedMs.coerceAtMost(endMs - 500L)
                            } else {
                                endMs = tappedMs.coerceAtLeast(startMs + 500L)
                            }
                        }
                    }
                    .pointerInput(totalDurationMs) {
                        detectDragGestures { change, _ ->
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            val dragMs = (ratio * totalDurationMs).toLong()
                            if (kotlin.math.abs(dragMs - startMs) < kotlin.math.abs(dragMs - endMs)) {
                                startMs = dragMs.coerceIn(0L, endMs - 500L)
                            } else {
                                endMs = dragMs.coerceIn(startMs + 500L, totalDurationMs)
                            }
                        }
                    }
            ) {
                val onBackgroundColor = MaterialTheme.colorScheme.onBackground
                val subtleColor = VoxTheme.colors.subtleText
                val dividerColor = VoxTheme.colors.divider

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barCount = waveformPeaks.size
                    if (barCount == 0) return@Canvas

                    val barWidth = width / barCount
                    val startRatio = (startMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                    val endRatio = (endMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                    val startX = startRatio * width
                    val endX = endRatio * width

                    // Highlight selected zone
                    drawRect(
                        color = onBackgroundColor.copy(alpha = 0.12f),
                        topLeft = Offset(startX, 0f),
                        size = Size(endX - startX, height)
                    )

                    // Draw waveform bars
                    for (i in 0 until barCount) {
                        val peak = waveformPeaks[i].coerceIn(0.05f, 1f)
                        val barHeight = peak * (height - 20.dp.toPx())
                        val x = i * barWidth + (barWidth * 0.2f)
                        val y = (height - barHeight) / 2f
                        val isInside = (i.toFloat() / barCount) in startRatio..endRatio

                        drawRect(
                            color = if (isInside) onBackgroundColor else subtleColor.copy(alpha = 0.4f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth * 0.6f, barHeight)
                        )
                    }

                    // Start Marker Line
                    drawLine(
                        color = onBackgroundColor,
                        start = Offset(startX, 0f),
                        end = Offset(startX, height),
                        strokeWidth = 2.dp.toPx()
                    )

                    // End Marker Line
                    drawLine(
                        color = onBackgroundColor,
                        start = Offset(endX, 0f),
                        end = Offset(endX, height),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Playhead Line
                    if (currentPreviewPositionMs in startMs..endMs) {
                        val playheadRatio = (currentPreviewPositionMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                        val playheadX = playheadRatio * width
                        drawLine(
                            color = Color.White,
                            start = Offset(playheadX, 0f),
                            end = Offset(playheadX, height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timestamps Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "START TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoxTheme.colors.subtleText
                    )
                    Text(
                        text = formatTime(startMs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CLIP DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoxTheme.colors.subtleText
                    )
                    Text(
                        text = formatTime(endMs - startMs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "END TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoxTheme.colors.subtleText
                    )
                    Text(
                        text = formatTime(endMs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Precision Stepper Controls
            Text(
                text = "PRECISION ADJUSTMENTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StepperButton(label = "START -1s", onClick = { startMs = (startMs - 1000L).coerceAtLeast(0L) }, modifier = Modifier.weight(1f))
                StepperButton(label = "START +1s", onClick = { startMs = (startMs + 1000L).coerceAtMost(endMs - 500L) }, modifier = Modifier.weight(1f))
                StepperButton(label = "END -1s", onClick = { endMs = (endMs - 1000L).coerceAtLeast(startMs + 500L) }, modifier = Modifier.weight(1f))
                StepperButton(label = "END +1s", onClick = { endMs = (endMs + 1000L).coerceAtMost(totalDurationMs) }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Range Preview Button
            Button(
                onClick = { onTogglePreview(startMs, endMs) },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = if (isPreviewPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Preview Clip",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPreviewPlaying) "PAUSE PREVIEW" else "PLAY SELECTED RANGE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportClipDialog(
            defaultName = "${track.title}_clip",
            durationFormatted = formatTime(endMs - startMs),
            onConfirmExport = { name ->
                onExportClip(startMs, endMs, name)
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun StepperButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
            .height(34.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun ExportClipDialog(
    defaultName: String,
    durationFormatted: String,
    onConfirmExport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var outputName by remember { mutableStateOf(defaultName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "EXPORT AUDIO CLIP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Clip Duration: $durationFormatted (Lossless Fast Extraction)",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoxTheme.colors.subtleText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Output File Name:",
                    style = MaterialTheme.typography.labelSmall,
                    color = VoxTheme.colors.subtleText
                )
                Spacer(modifier = Modifier.height(4.dp))

                BasicTextField(
                    value = outputName,
                    onValueChange = { outputName = it },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
                    ) {
                        Text(text = "CANCEL", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            if (outputName.isNotBlank()) {
                                onConfirmExport(outputName.trim())
                            }
                        },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = "SAVE CLIP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = ms % 1000 / 100
    return String.format("%02d:%02d.%d", minutes, seconds, millis)
}
