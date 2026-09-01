package com.vox.music.feature.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxCoverArt
import com.vox.music.ui.theme.VoxTheme

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Search

private enum class QueuePickerSort {
    A_TO_Z, DATE_ADDED, DURATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    queue: List<AudioTrack>,
    currentTrackId: Long?,
    allAvailableTracks: List<AudioTrack>,
    onTrackSelected: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveTrack: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onClearQueue: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddTrackPicker by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAYING QUEUE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${queue.size} tracks in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (queue.size > 1) {
                        IconButton(onClick = onClearQueue) {
                            Icon(
                                imageVector = Lucide.Eraser,
                                contentDescription = "Clear Queue (Keep Current)",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(onClick = { showAddTrackPicker = true }) {
                        Icon(
                            imageVector = Lucide.Plus,
                            contentDescription = "Add Track to Queue",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = "Close Queue",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(12.dp))

            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Lucide.Music,
                            contentDescription = null,
                            tint = VoxTheme.colors.subtleText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Queue is empty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = queue,
                        key = { index, track -> "${track.id}_$index" }
                    ) { index, track ->
                        val isCurrent = track.id == currentTrackId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCurrent) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.background
                                )
                                .clickable { onTrackSelected(index) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Track Index / Active indicator
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                                modifier = Modifier.width(22.dp)
                            )

                            // Thumbnail
                            VoxCoverArt(
                                filePath = track.filePath,
                                contentDescription = track.title,
                                shape = RoundedCornerShape(6.dp),
                                iconSize = 20.dp,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Metadata
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VoxTheme.colors.subtleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Reorder Up Button
                            if (index > 0) {
                                IconButton(
                                    onClick = { onMoveTrack(index, index - 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text(
                                        text = "▲",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VoxTheme.colors.subtleText
                                    )
                                }
                            }

                            // Reorder Down Button
                            if (index < queue.size - 1) {
                                IconButton(
                                    onClick = { onMoveTrack(index, index + 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text(
                                        text = "▼",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VoxTheme.colors.subtleText
                                    )
                                }
                            }

                            // Remove from Queue button
                            IconButton(
                                onClick = { onRemoveFromQueue(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.Trash2,
                                    contentDescription = "Remove from Queue",
                                    tint = VoxTheme.colors.subtleText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Track Picker Sheet with Search & Sort
    if (showAddTrackPicker) {
        var pickerQuery by remember { mutableStateOf("") }
        var pickerSort by remember { mutableStateOf(QueuePickerSort.A_TO_Z) }

        val filteredAndSortedTracks = remember(allAvailableTracks, pickerQuery, pickerSort) {
            val filtered = if (pickerQuery.isBlank()) {
                allAvailableTracks
            } else {
                allAvailableTracks.filter {
                    it.title.contains(pickerQuery, ignoreCase = true) ||
                    it.artist.contains(pickerQuery, ignoreCase = true) ||
                    it.album.contains(pickerQuery, ignoreCase = true)
                }
            }
            when (pickerSort) {
                QueuePickerSort.A_TO_Z -> filtered.sortedBy { it.title.lowercase() }
                QueuePickerSort.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
                QueuePickerSort.DURATION -> filtered.sortedByDescending { it.durationMs }
            }
        }

        val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddTrackPicker = false },
            sheetState = pickerSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "ADD TRACK TO QUEUE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Lucide.Search,
                            contentDescription = null,
                            tint = VoxTheme.colors.subtleText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = pickerQuery,
                            onValueChange = { pickerQuery = it },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (pickerQuery.isEmpty()) {
                                    Text(
                                        text = "Search songs to add...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VoxTheme.colors.subtleText
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (pickerQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { pickerQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.X,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sorting Chips (A-Z, Date, Duration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QueuePickerSort.entries.forEach { sortOption ->
                        val isSelected = pickerSort == sortOption
                        val label = when (sortOption) {
                            QueuePickerSort.A_TO_Z -> "A-Z"
                            QueuePickerSort.DATE_ADDED -> "Date Added"
                            QueuePickerSort.DURATION -> "Duration"
                        }
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.divider,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { pickerSort = sortOption }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
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

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(10.dp))

                if (filteredAndSortedTracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching tracks found",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(filteredAndSortedTracks, key = { _, t -> t.id }) { _, track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onAddTrackToQueue(track)
                                        showAddTrackPicker = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VoxCoverArt(
                                    filePath = track.filePath,
                                    contentDescription = track.title,
                                    shape = RoundedCornerShape(6.dp),
                                    iconSize = 20.dp,
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.artist} • ${track.durationFormatted}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VoxTheme.colors.subtleText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Lucide.Plus,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
