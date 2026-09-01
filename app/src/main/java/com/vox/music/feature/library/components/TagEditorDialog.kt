package com.vox.music.feature.library.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.theme.VoxTheme

import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditorDialog(
    initialTags: List<String>,
    onSaveTags: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val tags = remember { mutableStateListOf<String>().apply { addAll(initialTags) } }
    var newTagInput by remember { mutableStateOf("") }

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
                    text = "EDIT CUSTOM TAGS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Tags Chips (RoundedCornerShape 6.dp)
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
                            val formattedTag = if (tag.startsWith("#")) tag else "#$tag"
                            Row(
                                modifier = Modifier
                                    .border(0.5.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(6.dp))
                                    .padding(start = 8.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Lucide.X,
                                    contentDescription = "Remove Tag",
                                    tint = VoxTheme.colors.subtleText,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { tags.remove(tag) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Add new tag row (RoundedCornerShape 8.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, VoxTheme.colors.divider, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (newTagInput.isEmpty()) {
                                Text(
                                    text = "Add tag (e.g. Study, Focus)...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VoxTheme.colors.subtleText
                                )
                            }
                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = {
                            val cleanTag = newTagInput.trim().removePrefix("#")
                            if (cleanTag.isNotBlank() && cleanTag !in tags) {
                                tags.add(cleanTag)
                                newTagInput = ""
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.Plus,
                            contentDescription = "Add Tag",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (RoundedCornerShape 8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .border(0.5.dp, VoxTheme.colors.divider, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "CANCEL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onSaveTags(tags.toList())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = "SAVE TAGS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
