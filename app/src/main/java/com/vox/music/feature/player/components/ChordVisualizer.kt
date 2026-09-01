package com.vox.music.feature.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.core.model.ChordEvent
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

@Composable
fun ChordVisualizer(
    activeChord: ChordEvent?,
    upcomingChords: List<ChordEvent>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHORD TRACKER (REAL-TIME)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VoxTheme.colors.subtleText,
                letterSpacing = 1.sp
            )

            if (activeChord != null) {
                val timeSec = activeChord.timestampMs / 1000f
                Text(
                    text = "%.1fs".format(timeSec),
                    style = MaterialTheme.typography.labelSmall,
                    color = VoxTheme.colors.subtleText
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Big Active Chord Box
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeChord?.chord ?: "--",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Upcoming Chords Row
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "UPCOMING",
                    style = MaterialTheme.typography.labelSmall,
                    color = VoxTheme.colors.subtleText,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (upcomingChords.isEmpty()) {
                        Text(
                            text = "[ END OF TRACK ]",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTheme.colors.subtleText
                        )
                    } else {
                        upcomingChords.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.chord,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
