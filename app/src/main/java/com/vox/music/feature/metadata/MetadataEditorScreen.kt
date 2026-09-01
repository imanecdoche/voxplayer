package com.vox.music.feature.metadata

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vox.music.core.model.AudioMetadata
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

@Composable
fun MetadataEditorScreen(
    metadata: AudioMetadata,
    onSave: (AudioMetadata, ByteArray?) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf(metadata.title) }
    var artist by remember { mutableStateOf(metadata.artist) }
    var album by remember { mutableStateOf(metadata.album) }
    var albumArtist by remember { mutableStateOf(metadata.albumArtist) }
    var genre by remember { mutableStateOf(metadata.genre) }
    var year by remember { mutableStateOf(metadata.year) }
    var trackNumber by remember { mutableStateOf(metadata.trackNumber) }
    var discNumber by remember { mutableStateOf(metadata.discNumber) }
    var composer by remember { mutableStateOf(metadata.composer) }
    var comment by remember { mutableStateOf(metadata.comment) }
    var lyrics by remember { mutableStateOf(metadata.lyrics) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var newArtworkBytes by remember { mutableStateOf<ByteArray?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            context.contentResolver.openInputStream(it)?.use { stream ->
                newArtworkBytes = stream.readBytes()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "ID3 TAG & METADATA EDITOR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onBackground,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Button(
                    onClick = {
                        val updated = metadata.copy(
                            title = title.trim(),
                            artist = artist.trim(),
                            album = album.trim(),
                            albumArtist = albumArtist.trim(),
                            genre = genre.trim(),
                            year = year.trim(),
                            trackNumber = trackNumber.trim(),
                            discNumber = discNumber.trim(),
                            composer = composer.trim(),
                            comment = comment.trim(),
                            lyrics = lyrics.trim()
                        )
                        onSave(updated, newArtworkBytes)
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Check,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HairlineDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Artwork section (1:1 sharp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "New Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        metadata.artworkBytes != null -> {
                            AsyncImage(
                                model = metadata.artworkBytes,
                                contentDescription = "Current Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Lucide.Image,
                                contentDescription = "No Artwork",
                                tint = VoxTheme.colors.subtleText,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "COVER ARTWORK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Supports JPEG / PNG binary tag embedding",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.onBackground, RectangleShape)
                    ) {
                        Text(
                            text = "CHANGE ARTWORK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Text Input Fields
            MetadataInputField(label = "Title", value = title, onValueChange = { title = it })
            MetadataInputField(label = "Artist", value = artist, onValueChange = { artist = it })
            MetadataInputField(label = "Album", value = album, onValueChange = { album = it })
            MetadataInputField(label = "Album Artist", value = albumArtist, onValueChange = { albumArtist = it })
            MetadataInputField(label = "Genre", value = genre, onValueChange = { genre = it })
            MetadataInputField(label = "Year", value = year, onValueChange = { year = it })

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetadataInputField(label = "Track No", value = trackNumber, onValueChange = { trackNumber = it })
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetadataInputField(label = "Disc No", value = discNumber, onValueChange = { discNumber = it })
                }
            }

            MetadataInputField(label = "Composer", value = composer, onValueChange = { composer = it })
            MetadataInputField(label = "Comment", value = comment, onValueChange = { comment = it })
            MetadataInputField(label = "Lyrics (Unsynced USLT)", value = lyrics, onValueChange = { lyrics = it }, minLines = 4)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MetadataInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VoxTheme.colors.subtleText
        )
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            minLines = minLines,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, VoxTheme.colors.divider, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
