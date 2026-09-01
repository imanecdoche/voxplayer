package com.vox.music.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vox.music.ui.theme.VoxTheme

@Composable
fun VoxSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    trackHeight: Dp = 2.dp,
    thumbRadius: Dp = 6.dp,
    activeTrackColor: Color = MaterialTheme.colorScheme.onBackground,
    inactiveTrackColor: Color = VoxTheme.colors.divider,
    thumbColor: Color = MaterialTheme.colorScheme.onBackground
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val currentFraction = if (isDragging) {
        dragFraction
    } else {
        val range = valueRange.endInclusive - valueRange.start
        if (range > 0f) ((value - valueRange.start) / range).coerceIn(0f, 1f) else 0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = thumbRadius)
            .pointerInput(valueRange) {
                detectTapGestures(
                    onPress = { offset ->
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            val newFraction = (offset.x / width).coerceIn(0f, 1f)
                            val range = valueRange.endInclusive - valueRange.start
                            val newValue = valueRange.start + newFraction * range
                            onValueChange(newValue)
                            onValueChangeFinished()
                        }
                    }
                )
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            dragFraction = (offset.x / width).coerceIn(0f, 1f)
                            val range = valueRange.endInclusive - valueRange.start
                            onValueChange(valueRange.start + dragFraction * range)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        val range = valueRange.endInclusive - valueRange.start
                        val finalValue = valueRange.start + dragFraction * range
                        onValueChange(finalValue)
                        onValueChangeFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val width = size.width.toFloat()
                        if (width > 0f) {
                            dragFraction = (change.position.x / width).coerceIn(0f, 1f)
                            val range = valueRange.endInclusive - valueRange.start
                            onValueChange(valueRange.start + dragFraction * range)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val trackH = trackHeight.toPx()
            val thumbR = thumbRadius.toPx()

            val thumbX = (currentFraction * width).coerceIn(0f, width)

            // 1. Inactive Background Track (Full Width)
            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(0f, centerY - trackH / 2f),
                size = Size(width, trackH),
                cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
            )

            // 2. Active Foreground Track (from 0 to thumbX)
            if (thumbX > 0f) {
                drawRoundRect(
                    color = activeTrackColor,
                    topLeft = Offset(0f, centerY - trackH / 2f),
                    size = Size(thumbX, trackH),
                    cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
                )
            }

            // 3. Solid Circular Thumb
            drawCircle(
                color = thumbColor,
                radius = thumbR,
                center = Offset(thumbX, centerY)
            )
        }
    }
}
