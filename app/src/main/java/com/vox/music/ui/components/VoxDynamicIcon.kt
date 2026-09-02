package com.vox.music.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vox.music.core.theme.storage.ThemeFileManager
import com.vox.music.ui.theme.VoxTheme
import java.io.File

@Composable
fun VoxDynamicIcon(
    slotName: String,
    fallbackVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val activePackDir = VoxTheme.themeConfig.assets.customIconPackDirRelativePath

    val customFile: File? = remember(slotName, activePackDir) {
        val fileManager = ThemeFileManager(context)
        fileManager.getIconSlotFile(slotName, activePackDir)
    }

    val actualTint = if (tint != Color.Unspecified) tint else VoxTheme.themeConfig.colors.accent

    if (customFile != null && customFile.exists()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(customFile)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            colorFilter = if (actualTint != Color.Unspecified) ColorFilter.tint(actualTint) else null,
            modifier = modifier
                .size(size)
                .clipToBounds()
        )
    } else {
        Icon(
            imageVector = fallbackVector,
            contentDescription = contentDescription,
            tint = actualTint,
            modifier = modifier.size(size)
        )
    }
}
