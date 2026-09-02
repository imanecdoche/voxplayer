package com.vox.music.feature.customization

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.RotateCw
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shuffle
import com.composables.icons.lucide.Sliders
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.FileText
import com.vox.music.core.theme.model.AssetPathsConfig
import com.vox.music.core.theme.model.ColorSchemeConfig
import com.vox.music.core.theme.model.DimensionsConfig
import com.vox.music.core.theme.model.OffsetsConfig
import com.vox.music.core.theme.model.PaddingsConfig
import com.vox.music.core.theme.model.VoxThemeConfig
import com.vox.music.core.theme.model.toComposeColor
import com.vox.music.core.theme.repository.BackgroundSlot
import com.vox.music.core.theme.repository.ThemeRepository
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxSlider
import com.vox.music.ui.theme.VoxTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class adcuzActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeConfig by themeRepository.themeStateFlow.collectAsState()

            VoxTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdvancedCustomizationScreen(
                        themeConfig = themeConfig,
                        onUpdateTheme = { transform -> themeRepository.updateTheme(transform) },
                        onResetTheme = { themeRepository.resetToDefault() },
                        onIngestBg = { uri, slot -> themeRepository.ingestBackgroundImage(uri, slot) },
                        onClearBg = { slot -> themeRepository.clearBackgroundImage(slot) },
                        onIngestHeaderIcon = { uri -> themeRepository.ingestHeaderIcon(uri) },
                        onClearHeaderIcon = { themeRepository.clearHeaderIcon() },
                        onIngestSingleIcon = { slot, uri -> themeRepository.ingestSingleIcon(slot, uri) },
                        onImportIconPack = { uri -> themeRepository.importIconPack(uri) },
                        onClearIconPack = { themeRepository.clearIconPack() },
                        onExportTheme = { uri -> themeRepository.exportTheme(uri) },
                        onImportTheme = { uri -> themeRepository.importTheme(uri) },
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedCustomizationScreen(
    themeConfig: VoxThemeConfig,
    onUpdateTheme: ((VoxThemeConfig) -> VoxThemeConfig) -> Unit,
    onResetTheme: () -> Unit,
    onIngestBg: (Uri, BackgroundSlot) -> Result<String>,
    onClearBg: (BackgroundSlot) -> Unit,
    onIngestHeaderIcon: (Uri) -> Result<String>,
    onClearHeaderIcon: () -> Unit,
    onIngestSingleIcon: (String, Uri) -> Result<String>,
    onImportIconPack: (Uri) -> Result<String>,
    onClearIconPack: () -> Unit,
    onExportTheme: (Uri) -> Result<Unit>,
    onImportTheme: (Uri) -> Result<Unit>,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Colors", "Dimensions", "Assets", "Icons", "Themes")

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = "Back",
                    tint = themeConfig.colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "THEME STUDIO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = themeConfig.colors.textPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onResetTheme()
                    Toast.makeText(context, "Tema dikembalikan ke default", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Lucide.RotateCw,
                    contentDescription = "Reset Theme",
                    tint = themeConfig.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HairlineDivider()

        // 2. Top Section: LiveThemePreviewBox (Sticky ~33-35% Height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .background(themeConfig.colors.background)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            LiveThemePreviewBox(themeConfig = themeConfig)
        }

        HairlineDivider()

        // 3. TabRow Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = themeConfig.colors.background,
            contentColor = themeConfig.colors.accent,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 2.dp,
                    color = themeConfig.colors.accent
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) themeConfig.colors.textPrimary else themeConfig.colors.textSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                )
            }
        }

        HairlineDivider()

        // 4. Bottom Section: Tab Content (Weight 0.65f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .background(themeConfig.colors.background)
        ) {
            when (selectedTab) {
                0 -> TabColorsContent(themeConfig = themeConfig, onUpdateTheme = onUpdateTheme)
                1 -> TabDimensionsContent(themeConfig = themeConfig, onUpdateTheme = onUpdateTheme)
                2 -> TabAssetsContent(
                    themeConfig = themeConfig,
                    onIngestBg = onIngestBg,
                    onClearBg = onClearBg,
                    onIngestHeaderIcon = onIngestHeaderIcon,
                    onClearHeaderIcon = onClearHeaderIcon
                )
                3 -> TabIconsContent(
                    themeConfig = themeConfig,
                    onIngestSingleIcon = onIngestSingleIcon,
                    onImportIconPack = onImportIconPack,
                    onClearIconPack = onClearIconPack
                )
                4 -> TabThemesContent(
                    themeConfig = themeConfig,
                    onUpdateTheme = onUpdateTheme,
                    onResetTheme = onResetTheme,
                    onExportTheme = onExportTheme,
                    onImportTheme = onImportTheme
                )
            }
        }
    }
}

@Composable
private fun LiveThemePreviewBox(themeConfig: VoxThemeConfig) {
    val colors = themeConfig.colors
    val dim = themeConfig.dimensions
    val offsets = themeConfig.offsets
    val paddings = themeConfig.paddings

    var isPreviewPlaying by remember { mutableStateOf(true) }
    var previewSliderPos by remember { mutableFloatStateOf(0.42f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.background)
            .border(1.dp, colors.sliderTrackInactive, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE THEME PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "INTERACTIVE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = colors.accent,
                fontWeight = FontWeight.Medium
            )
        }

        // Preview Item 1: Floating Mini Player Capsule
        Box(
            modifier = Modifier
                .offset { IntOffset(offsets.capsuleOffsetX.dp.roundToPx(), offsets.capsuleOffsetY.dp.roundToPx()) }
                .fillMaxWidth()
                .padding(horizontal = paddings.capsulePaddingHorizontalDp.dp)
                .height(dim.capsuleHeightDp.dp)
                .clip(RoundedCornerShape(dim.capsuleCornerRadiusPercent))
                .background(colors.capsuleBackground)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular/Rounded Album Thumbnail
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, offsets.defaultMusicIconOffsetY.dp.roundToPx()) }
                        .size((dim.capsuleHeightDp - 14).dp)
                        .clip(RoundedCornerShape(dim.artworkRadiusDp.dp))
                        .background(colors.defaultArtworkBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.Music,
                        contentDescription = "Music",
                        tint = colors.defaultMusicIcon,
                        modifier = Modifier.size(dim.defaultMusicIconSizeDp.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vox Minimalist Track",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Dynamic Theme Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }

                // Controls Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { /* Preview click */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.SkipBack,
                            contentDescription = "Prev",
                            tint = colors.capsuleTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isPreviewPlaying = !isPreviewPlaying },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPreviewPlaying) Lucide.Pause else Lucide.Play,
                            contentDescription = "Play/Pause",
                            tint = colors.capsuleTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* Preview click */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.SkipForward,
                            contentDescription = "Next",
                            tint = colors.capsuleTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Preview Item 2: Custom Thin Seekbar
        Column(
            modifier = Modifier
                .offset { IntOffset(0, offsets.seekbarOffsetY.dp.roundToPx()) }
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "01:24",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "03:45",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            VoxSlider(
                value = previewSliderPos,
                onValueChange = { previewSliderPos = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Preview Item 3: 5 Main Playback Controls
        Row(
            modifier = Modifier
                .offset { IntOffset(0, offsets.playbackControlsOffsetY.dp.roundToPx()) }
                .fillMaxWidth()
                .scale(dim.playbackControlsScale)
                .padding(horizontal = paddings.controlsGapDp.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Lucide.Shuffle,
                    contentDescription = "Shuffle",
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Lucide.SkipBack,
                    contentDescription = "Prev",
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = { isPreviewPlaying = !isPreviewPlaying },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (isPreviewPlaying) Lucide.Pause else Lucide.Play,
                    contentDescription = "Play/Pause",
                    tint = colors.accent,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Lucide.SkipForward,
                    contentDescription = "Next",
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Lucide.Repeat,
                    contentDescription = "Repeat",
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ================================================================
// TAB 0: COLORS
// ================================================================
@Composable
private fun TabColorsContent(
    themeConfig: VoxThemeConfig,
    onUpdateTheme: ((VoxThemeConfig) -> VoxThemeConfig) -> Unit
) {
    var activeColorKey by remember { mutableStateOf<String?>(null) }
    var activeColorTitle by remember { mutableStateOf("") }
    var activeColorInitialHex by remember { mutableStateOf("#FFFFFF") }

    val colors = themeConfig.colors

    val colorItems = listOf(
        ColorEntry("textPrimaryHex", "Text Primary", colors.textPrimaryHex),
        ColorEntry("textSecondaryHex", "Text Secondary", colors.textSecondaryHex),
        ColorEntry("backgroundHex", "Background", colors.backgroundHex),
        ColorEntry("accentHex", "Accent / Highlight", colors.accentHex),
        ColorEntry("capsuleBackgroundHex", "Capsule Mini Player Bg", colors.capsuleBackgroundHex),
        ColorEntry("capsuleTintHex", "Capsule Controls Tint", colors.capsuleTintHex),
        ColorEntry("sliderTrackActiveHex", "Slider Track Active", colors.sliderTrackActiveHex),
        ColorEntry("sliderTrackInactiveHex", "Slider Track Inactive", colors.sliderTrackInactiveHex),
        ColorEntry("sliderThumbHex", "Slider Thumb", colors.sliderThumbHex),
        ColorEntry("toggleActiveHex", "Switch / Toggle Active", colors.toggleActiveHex),
        ColorEntry("toggleInactiveHex", "Switch / Toggle Inactive", colors.toggleInactiveHex),
        ColorEntry("appIconTintHex", "App Header Icon Tint", colors.appIconTintHex),
        ColorEntry("defaultArtworkBgHex", "Default Artwork Box Bg", colors.defaultArtworkBgHex),
        ColorEntry("defaultMusicIconHex", "Default Music Icon Color", colors.defaultMusicIconHex),
        ColorEntry("seekbarLineHex", "Seekbar Line", colors.seekbarLineHex)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        colorItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        activeColorKey = item.key
                        activeColorTitle = item.label
                        activeColorInitialHex = item.hex
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = item.hex,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(item.hex.toComposeColor())
                        .border(1.5.dp, colors.sliderTrackInactive, CircleShape)
                )
            }
            HairlineDivider()
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (activeColorKey != null) {
        ColorPickerDialog(
            title = activeColorTitle,
            initialHex = activeColorInitialHex,
            onColorSelected = { newHex ->
                onUpdateTheme { current ->
                    val updatedColors = when (activeColorKey) {
                        "textPrimaryHex" -> current.colors.copy(textPrimaryHex = newHex)
                        "textSecondaryHex" -> current.colors.copy(textSecondaryHex = newHex)
                        "backgroundHex" -> current.colors.copy(backgroundHex = newHex)
                        "accentHex" -> current.colors.copy(accentHex = newHex)
                        "capsuleBackgroundHex" -> current.colors.copy(capsuleBackgroundHex = newHex)
                        "capsuleTintHex" -> current.colors.copy(capsuleTintHex = newHex)
                        "sliderTrackActiveHex" -> current.colors.copy(sliderTrackActiveHex = newHex)
                        "sliderTrackInactiveHex" -> current.colors.copy(sliderTrackInactiveHex = newHex)
                        "sliderThumbHex" -> current.colors.copy(sliderThumbHex = newHex)
                        "toggleActiveHex" -> current.colors.copy(toggleActiveHex = newHex)
                        "toggleInactiveHex" -> current.colors.copy(toggleInactiveHex = newHex)
                        "appIconTintHex" -> current.colors.copy(appIconTintHex = newHex)
                        "defaultArtworkBgHex" -> current.colors.copy(defaultArtworkBgHex = newHex)
                        "defaultMusicIconHex" -> current.colors.copy(defaultMusicIconHex = newHex)
                        "seekbarLineHex" -> current.colors.copy(seekbarLineHex = newHex)
                        else -> current.colors
                    }
                    current.copy(colors = updatedColors)
                }
                activeColorKey = null
            },
            onDismiss = { activeColorKey = null }
        )
    }
}

private data class ColorEntry(val key: String, val label: String, val hex: String)

// ================================================================
// TAB 1: DIMENSIONS & POSITIONS
// ================================================================
@Composable
private fun TabDimensionsContent(
    themeConfig: VoxThemeConfig,
    onUpdateTheme: ((VoxThemeConfig) -> VoxThemeConfig) -> Unit
) {
    val dim = themeConfig.dimensions
    val offsets = themeConfig.offsets
    val paddings = themeConfig.paddings
    val colors = themeConfig.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --- 1. GLOBAL & CAPSULE ---
        SectionHeader("MINI PLAYER CAPSULE", colors.textSecondary)

        DimensionSliderRow(
            label = "Global Scale",
            value = dim.scaleGlobal,
            range = 0.5f..2.0f,
            format = "%.2fx",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(scaleGlobal = floatVal)) } }
        )

        DimensionSliderRow(
            label = "Capsule Height",
            value = dim.capsuleHeightDp.toFloat(),
            range = 40f..80f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(capsuleHeightDp = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Capsule Corner Radius",
            value = dim.capsuleCornerRadiusPercent.toFloat(),
            range = 0f..50f,
            format = "%.0f %%",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(capsuleCornerRadiusPercent = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Capsule Offset X",
            value = offsets.capsuleOffsetX.toFloat(),
            range = -40f..40f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(offsets = current.offsets.copy(capsuleOffsetX = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Capsule Offset Y",
            value = offsets.capsuleOffsetY.toFloat(),
            range = -40f..40f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(offsets = current.offsets.copy(capsuleOffsetY = floatVal.roundToInt())) } }
        )

        Spacer(modifier = Modifier.height(14.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. ARTWORK & ICONS ---
        SectionHeader("ARTWORK & THUMBNAILS", colors.textSecondary)

        DimensionSliderRow(
            label = "Artwork Corner Radius",
            value = dim.artworkRadiusDp.toFloat(),
            range = 0f..40f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(artworkRadiusDp = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Default Music Icon Size",
            value = dim.defaultMusicIconSizeDp.toFloat(),
            range = 16f..60f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(defaultMusicIconSizeDp = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Artwork Offset Y",
            value = offsets.artworkOffsetY.toFloat(),
            range = -30f..30f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(offsets = current.offsets.copy(artworkOffsetY = floatVal.roundToInt())) } }
        )

        Spacer(modifier = Modifier.height(14.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. SEEKBAR & CONTROLS ---
        SectionHeader("SEEKBAR & CONTROLS", colors.textSecondary)

        DimensionSliderRow(
            label = "Slider Line Thickness",
            value = dim.sliderThicknessDp.toFloat(),
            range = 1f..8f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(sliderThicknessDp = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Slider Thumb Diameter",
            value = dim.sliderThumbDiameterDp.toFloat(),
            range = 0f..20f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(sliderThumbDiameterDp = floatVal.roundToInt())) } }
        )

        DimensionSliderRow(
            label = "Playback Controls Scale",
            value = dim.playbackControlsScale,
            range = 0.5f..2.0f,
            format = "%.2fx",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(dimensions = current.dimensions.copy(playbackControlsScale = floatVal)) } }
        )

        DimensionSliderRow(
            label = "Controls Gap",
            value = paddings.controlsGapDp.toFloat(),
            range = 8f..48f,
            format = "%.0f dp",
            onValueChange = { floatVal -> onUpdateTheme { current -> current.copy(paddings = current.paddings.copy(controlsGapDp = floatVal.roundToInt())) } }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = color,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun DimensionSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = format.format(value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        VoxSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ================================================================
// TAB 2: ASSETS & BACKGROUNDS
// ================================================================
@Composable
private fun TabAssetsContent(
    themeConfig: VoxThemeConfig,
    onIngestBg: (Uri, BackgroundSlot) -> Result<String>,
    onClearBg: (BackgroundSlot) -> Unit,
    onIngestHeaderIcon: (Uri) -> Result<String>,
    onClearHeaderIcon: () -> Unit
) {
    val context = LocalContext.current
    var activeSlotToPick by remember { mutableStateOf<BackgroundSlot?>(null) }

    val bgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeSlotToPick != null) {
            val res = onIngestBg(uri, activeSlotToPick!!)
            if (res.isSuccess) {
                Toast.makeText(context, "Background berhasil disimpan (WEBP 85%)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
        activeSlotToPick = null
    }

    val headerIconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val res = onIngestHeaderIcon(uri)
            if (res.isSuccess) {
                Toast.makeText(context, "Custom Header Icon berhasil diperbarui", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val assets = themeConfig.assets
    val colors = themeConfig.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SectionHeader("CUSTOM SCREEN BACKGROUNDS", colors.textSecondary)

        AssetPickerCard(
            title = "Home Screen Background",
            relativePath = assets.homeBackgroundRelativePath,
            onPick = {
                activeSlotToPick = BackgroundSlot.HOME
                bgLauncher.launch("image/*")
            },
            onClear = { onClearBg(BackgroundSlot.HOME) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AssetPickerCard(
            title = "Tracklist / Library Background",
            relativePath = assets.tracklistBackgroundRelativePath,
            onPick = {
                activeSlotToPick = BackgroundSlot.TRACKLIST
                bgLauncher.launch("image/*")
            },
            onClear = { onClearBg(BackgroundSlot.TRACKLIST) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AssetPickerCard(
            title = "Player Screen Background",
            relativePath = assets.playerBackgroundRelativePath,
            onPick = {
                activeSlotToPick = BackgroundSlot.PLAYER
                bgLauncher.launch("image/*")
            },
            onClear = { onClearBg(BackgroundSlot.PLAYER) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AssetPickerCard(
            title = "Settings Screen Background",
            relativePath = assets.settingsBackgroundRelativePath,
            onPick = {
                activeSlotToPick = BackgroundSlot.SETTINGS
                bgLauncher.launch("image/*")
            },
            onClear = { onClearBg(BackgroundSlot.SETTINGS) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("APP BRANDING ASSET", colors.textSecondary)

        AssetPickerCard(
            title = "Custom App Header Icon (PNG/WEBP/SVG)",
            relativePath = assets.customHeaderIconRelativePath,
            onPick = { headerIconLauncher.launch("image/*") },
            onClear = onClearHeaderIcon
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AssetPickerCard(
    title: String,
    relativePath: String?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val file = remember(relativePath) {
        if (!relativePath.isNullOrBlank()) {
            File(context.filesDir, "theme_engine/$relativePath")
        } else null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141416))
            .border(1.dp, Color(0xFF28282A), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Thumbnail preview
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF222224)),
            contentAlignment = Alignment.Center
        ) {
            if (file != null && file.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(file).build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Lucide.Image,
                    contentDescription = null,
                    tint = Color(0xFF6E6E73),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = if (file != null && file.exists()) file.name else "Belum dipilih (Default)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8E8E93),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row {
            IconButton(onClick = onPick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = "Pilih",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (file != null && file.exists()) {
                IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Lucide.Trash2,
                        contentDescription = "Hapus",
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ================================================================
// TAB 3: ICONS
// ================================================================
@Composable
private fun TabIconsContent(
    themeConfig: VoxThemeConfig,
    onIngestSingleIcon: (String, Uri) -> Result<String>,
    onImportIconPack: (Uri) -> Result<String>,
    onClearIconPack: () -> Unit
) {
    val context = LocalContext.current
    var activeSlotToAssign by remember { mutableStateOf<String?>(null) }

    val singleIconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeSlotToAssign != null) {
            val res = onIngestSingleIcon(activeSlotToAssign!!, uri)
            if (res.isSuccess) {
                Toast.makeText(context, "Icon slot '${activeSlotToAssign}' berhasil diperbarui", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
        activeSlotToAssign = null
    }

    val iconPackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val res = onImportIconPack(uri)
            if (res.isSuccess) {
                Toast.makeText(context, "Icon pack berhasil diekstrak dan diaktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal impor icon pack: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val colors = themeConfig.colors
    val iconSlots = listOf(
        Pair("ic_play", "Play Button"),
        Pair("ic_pause", "Pause Button"),
        Pair("ic_next", "Next Track"),
        Pair("ic_prev", "Previous Track"),
        Pair("ic_shuffle", "Shuffle Button"),
        Pair("ic_repeat", "Repeat Mode"),
        Pair("ic_favorite", "Favorite Heart"),
        Pair("ic_folder", "Folder Icon"),
        Pair("ic_settings", "Settings Gear")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SectionHeader("BATCH ICON PACK (.ZIP / .VOXPACK)", colors.textSecondary)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141416))
                .border(1.dp, Color(0xFF28282A), RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Import Custom Icon Pack",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (themeConfig.assets.customIconPackDirRelativePath != null) "Active Pack: active_pack" else "Menggunakan Lucide icon bawaan",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E93)
                )
            }

            Row {
                Button(
                    onClick = { iconPackLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Lucide.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (themeConfig.assets.customIconPackDirRelativePath != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onClearIconPack, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Lucide.Trash2, contentDescription = "Clear", tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("INDIVIDUAL ICON SLOTS (PNG, WEBP, SVG)", colors.textSecondary)

        iconSlots.forEach { (slot, title) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        activeSlotToAssign = slot
                        singleIconLauncher.launch("image/*")
                    }
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text(text = "Slot: $slot", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                }

                Button(
                    onClick = {
                        activeSlotToAssign = slot
                        singleIconLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242426), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pilih", fontSize = 11.sp)
                }
            }
            HairlineDivider()
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ================================================================
// TAB 4: THEMES & BUNDLES
// ================================================================
@Composable
private fun TabThemesContent(
    themeConfig: VoxThemeConfig,
    onUpdateTheme: ((VoxThemeConfig) -> VoxThemeConfig) -> Unit,
    onResetTheme: () -> Unit,
    onExportTheme: (Uri) -> Result<Unit>,
    onImportTheme: (Uri) -> Result<Unit>
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            val res = onExportTheme(uri)
            if (res.isSuccess) {
                Toast.makeText(context, "Tema berhasil diekspor (.voxtheme)!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal mengekspor: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val res = onImportTheme(uri)
            if (res.isSuccess) {
                Toast.makeText(context, "Tema .voxtheme berhasil diimpor dan diaktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal mengimpor: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val colors = themeConfig.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SectionHeader("QUICK PRESET THEMES", colors.textSecondary)

        PresetCard(
            title = "AMOLED Black (Default)",
            subtitle = "Pure #000000, White accents, Minimalist typography",
            bgColor = Color.Black,
            accentColor = Color.White,
            onClick = {
                onUpdateTheme {
                    VoxThemeConfig(
                        colors = ColorSchemeConfig(
                            backgroundHex = "#000000",
                            textPrimaryHex = "#FFFFFF",
                            textSecondaryHex = "#757575",
                            accentHex = "#FFFFFF",
                            capsuleBackgroundHex = "#1E1E1E",
                            capsuleTintHex = "#FFFFFF"
                        )
                    )
                }
                Toast.makeText(context, "Tema AMOLED Black diterapkan", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PresetCard(
            title = "Pure White Minimal",
            subtitle = "Clean white paper canvas with deep black contrast",
            bgColor = Color.White,
            accentColor = Color.Black,
            onClick = {
                onUpdateTheme {
                    VoxThemeConfig(
                        colors = ColorSchemeConfig(
                            backgroundHex = "#FFFFFF",
                            textPrimaryHex = "#000000",
                            textSecondaryHex = "#757575",
                            accentHex = "#000000",
                            capsuleBackgroundHex = "#F0F0F0",
                            capsuleTintHex = "#000000",
                            sliderTrackActiveHex = "#000000",
                            sliderTrackInactiveHex = "#E0E0E0",
                            sliderThumbHex = "#000000",
                            toggleActiveHex = "#000000",
                            toggleInactiveHex = "#E0E0E0",
                            defaultArtworkBgHex = "#F0F0F0",
                            defaultMusicIconHex = "#757575",
                            seekbarLineHex = "#000000"
                        )
                    )
                }
                Toast.makeText(context, "Tema Pure White diterapkan", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PresetCard(
            title = "Cyberpunk Neon",
            subtitle = "High-voltage neon cyan, magenta & deep cosmic midnight",
            bgColor = Color(0xFF0D0E15),
            accentColor = Color(0xFF00F0FF),
            onClick = {
                onUpdateTheme {
                    VoxThemeConfig(
                        colors = ColorSchemeConfig(
                            backgroundHex = "#0D0E15",
                            textPrimaryHex = "#FFFFFF",
                            textSecondaryHex = "#8E929B",
                            accentHex = "#00F0FF",
                            capsuleBackgroundHex = "#161926",
                            capsuleTintHex = "#00F0FF",
                            sliderTrackActiveHex = "#00F0FF",
                            sliderTrackInactiveHex = "#262B40",
                            sliderThumbHex = "#FF007A",
                            toggleActiveHex = "#00F0FF",
                            toggleInactiveHex = "#262B40",
                            defaultArtworkBgHex = "#161926",
                            defaultMusicIconHex = "#00F0FF",
                            seekbarLineHex = "#00F0FF"
                        )
                    )
                }
                Toast.makeText(context, "Tema Cyberpunk Neon diterapkan", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PresetCard(
            title = "Retro Slate & Amber",
            subtitle = "Vintage warm amber tube audio glow on dark slate",
            bgColor = Color(0xFF181A1B),
            accentColor = Color(0xFFFFB300),
            onClick = {
                onUpdateTheme {
                    VoxThemeConfig(
                        colors = ColorSchemeConfig(
                            backgroundHex = "#181A1B",
                            textPrimaryHex = "#F5F5F5",
                            textSecondaryHex = "#9E9E9E",
                            accentHex = "#FFB300",
                            capsuleBackgroundHex = "#24272A",
                            capsuleTintHex = "#FFB300",
                            sliderTrackActiveHex = "#FFB300",
                            sliderTrackInactiveHex = "#373B3E",
                            sliderThumbHex = "#FFB300",
                            toggleActiveHex = "#FFB300",
                            toggleInactiveHex = "#373B3E",
                            defaultArtworkBgHex = "#24272A",
                            defaultMusicIconHex = "#FFB300",
                            seekbarLineHex = "#FFB300"
                        )
                    )
                }
                Toast.makeText(context, "Tema Retro Slate diterapkan", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("EXPORT & IMPORT BUNDLE (.VOXTHEME)", colors.textSecondary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { exportLauncher.launch("VoxCustomTheme.voxtheme") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(imageVector = Lucide.FileText, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Theme", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { importLauncher.launch("*/*") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(imageVector = Lucide.Plus, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Theme", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PresetCard(
    title: String,
    subtitle: String,
    bgColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141416))
            .border(1.dp, Color(0xFF28282A), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(2.dp, accentColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
        }

        Icon(imageVector = Lucide.Check, contentDescription = "Select", tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp))
    }
}
