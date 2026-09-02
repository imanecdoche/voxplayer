package com.vox.music.feature.player.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxCoverArt
import com.vox.music.ui.theme.VoxTheme

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

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Local state list to enable real-time fluid reordering animation during drag
    val localQueue = remember { mutableStateListOf<AudioTrack>() }

    val lazyListState = rememberLazyListState()

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var initialDragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var listHeightPx by remember { mutableFloatStateOf(0f) }
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }

    val edgeThresholdPx = with(density) { 60.dp.toPx() }
    val itemHeightPx = with(density) { 58.dp.toPx() }

    // Edge Auto-Scrolling continuous loop when dragging near top/bottom boundaries
    LaunchedEffect(draggingIndex, autoScrollSpeed) {
        if (draggingIndex != null && autoScrollSpeed != 0f) {
            while (isActive && draggingIndex != null && autoScrollSpeed != 0f) {
                lazyListState.scrollBy(autoScrollSpeed)

                val currIdx = draggingIndex ?: break
                val threshold = itemHeightPx * 0.5f

                if (autoScrollSpeed > 0f && currIdx < localQueue.size - 1) {
                    dragOffsetY += (autoScrollSpeed * 0.5f)
                    if (dragOffsetY > threshold) {
                        val nextIdx = currIdx + 1
                        val item = localQueue.removeAt(currIdx)
                        localQueue.add(nextIdx, item)
                        draggingIndex = nextIdx
                        dragOffsetY -= itemHeightPx
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                } else if (autoScrollSpeed < 0f && currIdx > 0) {
                    dragOffsetY += (autoScrollSpeed * 0.5f)
                    if (dragOffsetY < -threshold) {
                        val prevIdx = currIdx - 1
                        val item = localQueue.removeAt(currIdx)
                        localQueue.add(prevIdx, item)
                        draggingIndex = prevIdx
                        dragOffsetY += itemHeightPx
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }

                delay(16L) // ~60fps
            }
        }
    }

    // Synchronize localQueue with incoming queue when not actively dragging
    LaunchedEffect(queue) {
        if (draggingIndex == null) {
            localQueue.clear()
            localQueue.addAll(queue)
        }
    }

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
                        text = "${localQueue.size} tracks in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (localQueue.size > 1) {
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

            if (localQueue.isEmpty()) {
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
                    state = lazyListState,
                    userScrollEnabled = draggingIndex == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            listHeightPx = coordinates.size.height.toFloat()
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = localQueue,
                        key = { _, track -> track.id }
                    ) { index, track ->
                        val isCurrent = track.id == currentTrackId
                        val isDraggingThis = draggingIndex == index

                        val scale by animateFloatAsState(
                            targetValue = if (isDraggingThis) 1.03f else 1.0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "dragScale"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .zIndex(if (isDraggingThis) 2f else 0f)
                                .graphicsLayer {
                                    translationY = if (isDraggingThis) dragOffsetY else 0f
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = if (isDraggingThis) 16f else 0f
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isDraggingThis -> MaterialTheme.colorScheme.surfaceVariant
                                        isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.background
                                    }
                                )
                                .then(
                                    if (isDraggingThis) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    } else Modifier
                                )
                                .clickable {
                                    if (draggingIndex == null) {
                                        onTrackSelected(index)
                                    }
                                }
                                .pointerInput(index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingIndex = index
                                            initialDragIndex = index
                                            dragOffsetY = 0f
                                            autoScrollSpeed = 0f
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                            val currIdx = draggingIndex ?: return@detectDragGesturesAfterLongPress

                                            // Calculate current dragged item center position within the LazyColumn viewport
                                            val visibleItem = lazyListState.layoutInfo.visibleItemsInfo.find { it.index == currIdx }
                                            val itemTopInList = visibleItem?.offset?.toFloat() ?: (currIdx * itemHeightPx)
                                            val currentItemCenterY = itemTopInList + dragOffsetY + (itemHeightPx / 2f)

                                            // Edge Auto-Scroll threshold calculation
                                            if (currentItemCenterY < edgeThresholdPx && listHeightPx > 0f) {
                                                val ratio = (1f - (currentItemCenterY / edgeThresholdPx).coerceIn(0f, 1f))
                                                autoScrollSpeed = -((ratio * with(density) { 16.dp.toPx() }).coerceAtLeast(with(density) { 3.dp.toPx() }))
                                            } else if (currentItemCenterY > (listHeightPx - edgeThresholdPx) && listHeightPx > 0f) {
                                                val distFromBottom = listHeightPx - currentItemCenterY
                                                val ratio = (1f - (distFromBottom / edgeThresholdPx).coerceIn(0f, 1f))
                                                autoScrollSpeed = (ratio * with(density) { 16.dp.toPx() }).coerceAtLeast(with(density) { 3.dp.toPx() })
                                            } else {
                                                autoScrollSpeed = 0f
                                            }

                                            val threshold = itemHeightPx * 0.5f

                                            if (dragOffsetY > threshold && currIdx < localQueue.size - 1) {
                                                val nextIdx = currIdx + 1
                                                val item = localQueue.removeAt(currIdx)
                                                localQueue.add(nextIdx, item)
                                                draggingIndex = nextIdx
                                                dragOffsetY -= itemHeightPx
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } else if (dragOffsetY < -threshold && currIdx > 0) {
                                                val prevIdx = currIdx - 1
                                                val item = localQueue.removeAt(currIdx)
                                                localQueue.add(prevIdx, item)
                                                draggingIndex = prevIdx
                                                dragOffsetY += itemHeightPx
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        },
                                        onDragEnd = {
                                            autoScrollSpeed = 0f
                                            val start = initialDragIndex
                                            val end = draggingIndex
                                            draggingIndex = null
                                            initialDragIndex = null
                                            dragOffsetY = 0f
                                            if (start != null && end != null && start != end) {
                                                onMoveTrack(start, end)
                                            }
                                        },
                                        onDragCancel = {
                                            autoScrollSpeed = 0f
                                            draggingIndex = null
                                            initialDragIndex = null
                                            dragOffsetY = 0f
                                            localQueue.clear()
                                            localQueue.addAll(queue)
                                        }
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drag Handle (GripVertical)
                            Icon(
                                imageVector = Lucide.GripVertical,
                                contentDescription = "Hold and Drag to Reorder",
                                tint = if (isDraggingThis) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )

                            // Track Index / Active indicator
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText,
                                modifier = Modifier.width(20.dp)
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
