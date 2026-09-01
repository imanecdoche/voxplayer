package com.vox.music.feature.library.components

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vox.music.core.model.Playlist
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onSelectPlaylist: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "ADD TO PLAYLIST",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(12.dp))

                if (isCreatingNew) {
                    Text(
                        text = "New Playlist Name:",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, VoxTheme.colors.divider, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isCreatingNew = false },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .border(0.5.dp, VoxTheme.colors.divider, RoundedCornerShape(8.dp))
                        ) {
                            Text(text = "CANCEL", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    onCreatePlaylist(newPlaylistName.trim())
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(text = "CREATE", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    // Create New Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCreatingNew = true }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Lucide.Plus,
                            contentDescription = "Create New Playlist",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CREATE NEW PLAYLIST...",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    HairlineDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (playlists.isEmpty()) {
                        Text(
                            text = "No custom playlists created yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTheme.colors.subtleText,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(playlists, key = { it.id }) { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectPlaylist(playlist.id)
                                            onDismiss()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Lucide.ListMusic,
                                            contentDescription = "Playlist",
                                            tint = VoxTheme.colors.subtleText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Text(
                                        text = "${playlist.trackCount} tracks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VoxTheme.colors.subtleText
                                    )
                                }
                                HairlineDivider()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(0.5.dp, VoxTheme.colors.divider, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "CLOSE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
