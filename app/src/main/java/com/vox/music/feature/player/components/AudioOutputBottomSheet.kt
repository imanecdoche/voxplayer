package com.vox.music.feature.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Battery
import com.composables.icons.lucide.Bluetooth
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Tv
import com.composables.icons.lucide.X
import com.vox.music.core.audio.routing.AudioRouteState
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOutputBottomSheet(
    routeState: AudioRouteState,
    onRouteToSpeaker: () -> Unit,
    onRouteToBluetooth: () -> Unit,
    onToggleDolbyAtmos: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUDIO OUTPUT & EFFECTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // A. Connected Bluetooth Device Info Card
            if (routeState.isBluetoothConnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, VoxTheme.colors.divider, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.Bluetooth,
                            contentDescription = "Bluetooth Device",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = routeState.bluetoothDeviceName.ifBlank { "Bluetooth Audio Device" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            // Battery Percentage Indicator (Only shown if valid battery level reported 0..100)
                            if (routeState.bluetoothBatteryLevel in 0..100) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(0.5.dp, VoxTheme.colors.divider, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Lucide.Battery,
                                        contentDescription = "Battery Level",
                                        tint = if (routeState.bluetoothBatteryLevel <= 20) Color(0xFFFF453A) else MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${routeState.bluetoothBatteryLevel}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (routeState.bluetoothBatteryLevel <= 20) Color(0xFFFF453A) else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34C759))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connected (Audio Playback)",
                                style = MaterialTheme.typography.labelSmall,
                                color = VoxTheme.colors.subtleText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // B. Audio Output Switcher (Bluetooth vs Phone Speaker)
            Text(
                text = "PLAYBACK OUTPUT ROUTING",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VoxTheme.colors.subtleText,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 1: Bluetooth
            if (routeState.isBluetoothConnected) {
                val isBtActive = !routeState.isRoutingToSpeaker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isBtActive) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.background
                        )
                        .clickable { onRouteToBluetooth() }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Lucide.Bluetooth,
                            contentDescription = null,
                            tint = if (isBtActive) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bluetooth (${routeState.bluetoothDeviceName.ifBlank { "Headset" }})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isBtActive) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isBtActive) "Active Output" else "Tap to route audio",
                                style = MaterialTheme.typography.labelSmall,
                                color = VoxTheme.colors.subtleText
                            )
                        }
                    }

                    if (isBtActive) {
                        Icon(
                            imageVector = Lucide.Check,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Option 2: Phone Speaker
            val isSpeakerActive = routeState.isRoutingToSpeaker || !routeState.isBluetoothConnected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSpeakerActive) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.background
                    )
                    .clickable { onRouteToSpeaker() }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Lucide.Tv,
                        contentDescription = null,
                        tint = if (isSpeakerActive) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phone Speaker (Speaker Ponsel)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSpeakerActive) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isSpeakerActive) "Active Output" else "Tap to route audio to phone speaker",
                            style = MaterialTheme.typography.labelSmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }

                if (isSpeakerActive) {
                    Icon(
                        imageVector = Lucide.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // C. Dolby Atmos / Spatial Audio Toggle
            Text(
                text = "SPATIAL AUDIO & ENHANCEMENT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VoxTheme.colors.subtleText,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Activity,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dolby Atmos / Spatial Audio",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (routeState.isDolbyAtmosAvailable || routeState.isSpatialAudioAvailable) {
                                if (routeState.isDolbyAtmosEnabled) "Immersive 3D audio enabled" else "Enhanced spatial surround"
                            } else {
                                "Not supported on this device"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }

                if (routeState.isDolbyAtmosAvailable || routeState.isSpatialAudioAvailable) {
                    Switch(
                        checked = routeState.isDolbyAtmosEnabled,
                        onCheckedChange = { onToggleDolbyAtmos(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                            uncheckedThumbColor = VoxTheme.colors.subtleText,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
