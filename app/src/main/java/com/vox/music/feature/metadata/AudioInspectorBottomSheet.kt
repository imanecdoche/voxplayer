package com.vox.music.feature.metadata

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vox.music.core.model.AudioMetadata
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioInspectorBottomSheet(
    metadata: AudioMetadata,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "AUDIO SIGNAL & FILE INSPECTOR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(12.dp))

            InspectorRow(label = "Format Codec", value = metadata.format)
            InspectorRow(label = "Bitrate", value = if (metadata.bitrateKbps > 0) "${metadata.bitrateKbps} kbps" else "Unknown")
            InspectorRow(label = "Sample Rate", value = if (metadata.sampleRateHz > 0) "${metadata.sampleRateHz} Hz" else "Unknown")
            InspectorRow(label = "Channels", value = if (metadata.channels == 2) "Stereo (2 Ch)" else if (metadata.channels == 1) "Mono (1 Ch)" else "${metadata.channels} Ch")
            InspectorRow(label = "Exact Duration", value = "${metadata.durationFormatted} (${metadata.durationMs} ms)")
            InspectorRow(label = "BPM / Tempo", value = if (metadata.bpm != null) "${metadata.bpm.toInt()} BPM" else "Analyzing / None")
            InspectorRow(label = "Musical Key", value = metadata.musicalKey ?: "Analyzing / None")
            InspectorRow(label = "File Size", value = metadata.fileSizeFormatted)
            InspectorRow(label = "File Path", value = metadata.filePath)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InspectorRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = VoxTheme.colors.subtleText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
