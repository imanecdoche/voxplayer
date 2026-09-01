package com.vox.music.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vox.music.R
import com.vox.music.ui.theme.VoxTheme

/**
 * Hairline Divider (0.5.dp default) for monochrome flat separation.
 */
@Composable
fun HairlineDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 0.5.dp,
    color: Color = VoxTheme.colors.divider
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

/**
 * Scalable Logomark Vector for Vox.
 */
@Composable
fun VoxLogomark(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    size: Dp = 24.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.vox_main_logomark),
        contentDescription = "Vox Logomark",
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * Scalable Logotype Vector for Vox.
 */
@Composable
fun VoxLogotype(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    height: Dp = 18.dp
) {
    // Aspect ratio of vox_main_logotype is 614:99 (~6.2:1)
    val width = height * (614f / 99f)
    Icon(
        painter = painterResource(id = R.drawable.vox_main_logotype),
        contentDescription = "VOX PLAYER",
        modifier = modifier
            .height(height)
            .width(width),
        tint = tint
    )
}

/**
 * Brand Header for Vox with Logomark + Logotype and optional trailing actions.
 */
@Composable
fun VoxHeader(
    modifier: Modifier = Modifier,
    showLogomark: Boolean = true,
    showLogotype: Boolean = true,
    title: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showLogomark) {
                VoxLogomark(size = 20.dp)
                if (showLogotype || title != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            if (showLogotype) {
                VoxLogotype(height = 16.dp)
            } else if (title != null) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

/**
 * Top header typography without any elevated surface or card.
 */
@Composable
fun MonochromeHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    VoxHeader(
        modifier = modifier,
        showLogomark = false,
        showLogotype = false,
        title = title,
        trailingContent = trailingContent
    )
}

/**
 * Album art display strictly adhering to the 1:1 sharp corners rule (no rounded corners).
 */
@Composable
fun SharpCoverArt(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    borderWidth: Dp = 0.5.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .border(borderWidth, VoxTheme.colors.divider, RectangleShape)
            .background(MaterialTheme.colorScheme.surface, RectangleShape)
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
    }
}
