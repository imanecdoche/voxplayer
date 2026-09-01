package com.vox.music.feature.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpeedPitchSheet(
    speed: Float,
    pitchSemitones: Int,
    pointA: Long? = null,
    pointB: Long? = null,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Int) -> Unit,
    onSetPointA: () -> Unit = {},
    onSetPointB: () -> Unit = {},
    onClearABLoop: () -> Unit = {},
    onResetSpeed: () -> Unit,
    onResetPitch: () -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val speedPresets = listOf(0.50f, 0.75f, 0.90f, 1.00f, 1.10f, 1.25f, 1.50f, 2.00f)
    val pitchPresets = listOf(-12, -7, -2, -1, 0, 1, 2, 7, 12)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RectangleShape,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DSP AUDIO PROCESSOR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "RESET ALL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .clickable(onClick = onResetAll)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ==================== PLAYBACK SPEED SECTION ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYBACK SPEED",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VoxTheme.colors.subtleText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.2fx".format(speed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (speed != 1.0f) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[1.0x]",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VoxTheme.colors.subtleText,
                            modifier = Modifier
                                .clickable(onClick = onResetSpeed)
                                .padding(2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slider with -0.05 / +0.05 step buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onSpeedChange(((speed - 0.05f) * 100).roundToInt() / 100f) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Minus,
                        contentDescription = "Decrease Speed",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Slider(
                    value = speed,
                    onValueChange = { onSpeedChange(((it * 100).roundToInt()) / 100f) },
                    valueRange = 0.25f..3.00f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = VoxTheme.colors.divider
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onSpeedChange(((speed + 0.05f) * 100).roundToInt() / 100f) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Plus,
                        contentDescription = "Increase Speed",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Speed Preset Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speedPresets.forEach { preset ->
                    val isSelected = (speed * 100).roundToInt() == (preset * 100).roundToInt()
                    Box(
                        modifier = Modifier
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.divider,
                                shape = RectangleShape
                            )
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                                shape = RectangleShape
                            )
                            .clickable { onSpeedChange(preset) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "%.2fx".format(preset),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ==================== PITCH SHIFTER SECTION ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PITCH SHIFTER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VoxTheme.colors.subtleText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (pitchSemitones > 0) "+$pitchSemitones st" else "$pitchSemitones st",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (pitchSemitones != 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[0 st]",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VoxTheme.colors.subtleText,
                            modifier = Modifier
                                .clickable(onClick = onResetPitch)
                                .padding(2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slider with -1 / +1 step buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onPitchChange(pitchSemitones - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Minus,
                        contentDescription = "Decrease Pitch",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Slider(
                    value = pitchSemitones.toFloat(),
                    onValueChange = { onPitchChange(it.toInt()) },
                    valueRange = -12f..12f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = VoxTheme.colors.divider
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onPitchChange(pitchSemitones + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Plus,
                        contentDescription = "Increase Pitch",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Pitch Preset Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pitchPresets.forEach { preset ->
                    val isSelected = pitchSemitones == preset
                    val label = when (preset) {
                        -12 -> "-12 (Oct↓)"
                        12 -> "+12 (Oct↑)"
                        0 -> "0 (Normal)"
                        else -> if (preset > 0) "+$preset st" else "$preset st"
                    }
                    Box(
                        modifier = Modifier
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.divider,
                                shape = RectangleShape
                            )
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                                shape = RectangleShape
                            )
                            .clickable { onPitchChange(preset) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ==================== A-B LOOP PRECISION CONTROLS ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A-B PRECISION LOOP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VoxTheme.colors.subtleText
                )

                if (pointA != null || pointB != null) {
                    Text(
                        text = "CLEAR LOOP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable(onClick = onClearABLoop)
                            .padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val aText = if (pointA != null) "POINT A: %02d:%02d".format((pointA / 1000) / 60, (pointA / 1000) % 60) else "SET POINT A"
                val bText = if (pointB != null) "POINT B: %02d:%02d".format((pointB / 1000) / 60, (pointB / 1000) % 60) else "SET POINT B"

                Button(
                    onClick = onSetPointA,
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pointA != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                        contentColor = if (pointA != null) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
                ) {
                    Text(
                        text = aText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onSetPointB,
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pointB != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                        contentColor = if (pointB != null) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
                ) {
                    Text(
                        text = bText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Done / Dismiss Button
            Button(
                onClick = onDismiss,
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = "APPLY & CLOSE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
