package com.vox.music.feature.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.core.model.AudioTrack
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackActionsSheet(
    track: AudioTrack,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onEditTags: () -> Unit,
    onEditMetadata: () -> Unit,
    onClipAudio: () -> Unit,
    onRenameFile: () -> Unit,
    onInspectAudio: () -> Unit,
    onDeleteFile: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        ) {
            // Header with Track Title & Artist
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${track.artist} • ${track.durationFormatted} • ${track.mimeType.substringAfterLast('/').uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoxTheme.colors.subtleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (track.customTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.customTags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(6.dp))

            // Actions List
            ActionMenuItem(
                icon = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = if (track.isFavorite) "Remove from Favorites" else "Add to Favorites",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            ActionMenuItem(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                label = "Add to Playlist...",
                onClick = {
                    onDismiss()
                    onAddToPlaylist()
                }
            )

            ActionMenuItem(
                icon = Icons.AutoMirrored.Filled.Label,
                label = "Edit Custom Tags...",
                onClick = {
                    onDismiss()
                    onEditTags()
                }
            )

            ActionMenuItem(
                icon = Icons.Filled.DriveFileRenameOutline,
                label = "Edit ID3 Metadata & Tags...",
                onClick = {
                    onDismiss()
                    onEditMetadata()
                }
            )

            ActionMenuItem(
                icon = Icons.Filled.ContentCut,
                label = "Clip & Trim Audio...",
                onClick = {
                    onDismiss()
                    onClipAudio()
                }
            )

            ActionMenuItem(
                icon = Icons.Filled.DriveFileRenameOutline,
                label = "Rename File...",
                onClick = {
                    onDismiss()
                    onRenameFile()
                }
            )

            ActionMenuItem(
                icon = Icons.Filled.Info,
                label = "Audio File & Signal Inspector",
                onClick = {
                    onDismiss()
                    onInspectAudio()
                }
            )

            ActionMenuItem(
                icon = Icons.Filled.DeleteOutline,
                label = "Delete from Storage",
                onClick = {
                    onDismiss()
                    onDeleteFile()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
