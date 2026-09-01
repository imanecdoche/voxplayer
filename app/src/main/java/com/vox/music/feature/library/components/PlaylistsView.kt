package com.vox.music.feature.library.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vox.music.core.model.Playlist
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun PlaylistsView(
    playlists: List<Playlist>,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Create Playlist Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCreateNewPlaylist)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = "New Playlist",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "NEW PLAYLIST...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        HairlineDivider()

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom playlists created yet.\nTap 'New Playlist' to organize your music.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoxTheme.colors.subtleText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${playlists.size} PLAYLISTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                    HairlineDivider()
                }

                items(playlists, key = { it.id }) { playlist ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPlaylist(playlist) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Lucide.ListMusic,
                                contentDescription = "Playlist",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${playlist.trackCount} tracks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VoxTheme.colors.subtleText
                                )
                            }

                            IconButton(
                                onClick = { onDeletePlaylist(playlist.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.Trash2,
                                    contentDescription = "Delete Playlist",
                                    tint = VoxTheme.colors.subtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                imageVector = Lucide.ChevronRight,
                                contentDescription = "Open",
                                tint = VoxTheme.colors.subtleText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HairlineDivider()
                    }
                }
            }
        }
    }
}
