package com.vox.music.feature.equalizer

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vox.music.core.audio.equalizer.EqualizerBand
import com.vox.music.core.audio.equalizer.EqualizerController
import com.vox.music.core.audio.equalizer.EqualizerState
import com.vox.music.feature.player.PlayerViewModel
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme
import kotlin.math.roundToInt

import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqualizerScreen(
    equalizerState: EqualizerState,
    builtInPresets: List<String>,
    onToggleEnabled: (Boolean) -> Unit,
    onSetBandLevel: (Short, Short) -> Unit,
    onApplyPreset: (String) -> Unit,
    onSetBassBoost: (Short) -> Unit,
    onSetVirtualizer: (Short) -> Unit,
    onSetLoudness: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "GRAPHIC EQUALIZER",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )

            // Flat Switch (RoundedCornerShape 6.dp)
            Box(
                modifier = Modifier
                    .border(
                        0.5.dp,
                        if (equalizerState.isEnabled) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.divider,
                        RoundedCornerShape(6.dp)
                    )
                    .background(
                        if (equalizerState.isEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleEnabled(!equalizerState.isEnabled) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (equalizerState.isEnabled) "ON" else "OFF",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (equalizerState.isEnabled) MaterialTheme.colorScheme.background else VoxTheme.colors.subtleText
                )
            }
        }

        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Preset Chips (RoundedCornerShape 6.dp)
        Text(
            text = "PRESETS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VoxTheme.colors.subtleText
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            builtInPresets.forEach { preset ->
                val isSelected = equalizerState.currentPresetName == preset
                Box(
                    modifier = Modifier
                        .border(
                            0.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.divider,
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = equalizerState.isEnabled) { onApplyPreset(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            !equalizerState.isEnabled -> VoxTheme.colors.subtleText
                            isSelected -> MaterialTheme.colorScheme.background
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // ==================== MULTI-BAND SLIDERS ====================
        Text(
            text = "FREQUENCY BANDS (${equalizerState.bands.size} BANDS)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VoxTheme.colors.subtleText
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (equalizerState.bands.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                equalizerState.bands.forEach { band ->
                    BandSliderRow(
                        band = band,
                        isEnabled = equalizerState.isEnabled,
                        onLevelChange = { levelMb -> onSetBandLevel(band.index, levelMb) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // ==================== AUDIO EFFECTS ====================
        Text(
            text = "AUDIO ENHANCEMENT EFFECTS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VoxTheme.colors.subtleText
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Bass Boost
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BASS BOOST",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${(equalizerState.bassBoostStrength / 10f).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Slider(
            value = equalizerState.bassBoostStrength.toFloat(),
            onValueChange = { onSetBassBoost(it.toInt().toShort()) },
            valueRange = 0f..1000f,
            enabled = equalizerState.isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = VoxTheme.colors.divider
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Virtualizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "VIRTUALIZER (3D SPATIAL)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${(equalizerState.virtualizerStrength / 10f).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Slider(
            value = equalizerState.virtualizerStrength.toFloat(),
            onValueChange = { onSetVirtualizer(it.toInt().toShort()) },
            valueRange = 0f..1000f,
            enabled = equalizerState.isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = VoxTheme.colors.divider
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Loudness Enhancer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LOUDNESS ENHANCER",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "+%.1f dB".format(equalizerState.loudnessGainMb / 100f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Slider(
            value = equalizerState.loudnessGainMb.toFloat(),
            onValueChange = { onSetLoudness(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = equalizerState.isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = VoxTheme.colors.divider
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BandSliderRow(
    band: EqualizerBand,
    isEnabled: Boolean,
    onLevelChange: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = band.centerFreqFormatted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
            modifier = Modifier.width(64.dp)
        )

        Slider(
            value = band.currentLevelMb.toFloat(),
            onValueChange = { onLevelChange(it.toInt().toShort()) },
            valueRange = band.minLevelMb.toFloat()..band.maxLevelMb.toFloat(),
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = VoxTheme.colors.divider
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        val gainText = if (band.currentGainDb > 0) "+%.1fdB".format(band.currentGainDb) else "%.1fdB".format(band.currentGainDb)
        Text(
            text = gainText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
            textAlign = TextAlign.End,
            modifier = Modifier.width(56.dp)
        )
    }
}
