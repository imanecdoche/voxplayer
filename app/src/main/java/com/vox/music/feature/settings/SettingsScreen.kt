package com.vox.music.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sliders
import com.vox.music.feature.customization.adcuzActivity
import com.vox.music.ui.components.HairlineDivider
import com.vox.music.ui.components.VoxSlider
import com.vox.music.ui.motion.VoxEasingCurve
import com.vox.music.ui.theme.VoxTheme
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("vox_prefs", Context.MODE_PRIVATE) }

    var crossfadeEnabled by remember {
        mutableStateOf(prefs.getBoolean("crossfade_enabled", false))
    }
    var crossfadeDurationS by remember {
        mutableFloatStateOf(prefs.getFloat("crossfade_duration_s", 1.5f))
    }

    var dynamicBgEnabled by remember {
        mutableStateOf(prefs.getBoolean("dynamic_background_enabled", true))
    }
    var dynamicBgIntensity by remember {
        mutableFloatStateOf(prefs.getFloat("dynamic_background_intensity", 0.22f))
    }

    var motionBlurEnabled by remember {
        mutableStateOf(prefs.getBoolean("motion_blur_enabled", false))
    }
    var motionBlurIntensity by remember {
        mutableFloatStateOf(prefs.getFloat("motion_blur_intensity", 5.0f))
    }

    val initialCurveStr = remember(prefs) {
        prefs.getString("animation_easing_curve", VoxEasingCurve.EASE_IN_OUT.name) ?: VoxEasingCurve.EASE_IN_OUT.name
    }
    var selectedEasing by remember {
        mutableStateOf(
            try {
                VoxEasingCurve.valueOf(initialCurveStr)
            } catch (e: Exception) {
                VoxEasingCurve.EASE_IN_OUT
            }
        )
    }
    var animationDurationMs by remember {
        mutableIntStateOf(prefs.getInt("animation_duration_ms", 300))
    }
    var showEasingDialog by remember { mutableStateOf(false) }

    var lockscreenPlayerEnabled by remember {
        mutableStateOf(prefs.getBoolean("lockscreen_player_enabled", true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Minimal Monochrome TopAppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = "Navigate Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "SETTINGS & PREFERENCES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
        }

        HairlineDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // === 1. PLAYBACK CATEGORY ===
            Text(
                text = "PLAYBACK",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = VoxTheme.colors.subtleText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Crossfade Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newVal = !crossfadeEnabled
                        crossfadeEnabled = newVal
                        prefs.edit().putBoolean("crossfade_enabled", newVal).apply()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Audio Crossfade",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Fade out track saat ini dan fade in track berikutnya untuk transisi antar lagu yang mulus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                Switch(
                    checked = crossfadeEnabled,
                    onCheckedChange = {
                        crossfadeEnabled = it
                        prefs.edit().putBoolean("crossfade_enabled", it).apply()
                    }
                )
            }

            // Crossfade Duration Custom Slider (0.5s - 3.0s, step 0.5s)
            if (crossfadeEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Crossfade Duration",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "%.1fs".format(crossfadeDurationS),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    VoxSlider(
                        value = crossfadeDurationS,
                        onValueChange = {
                            // Step by 0.5s
                            val stepped = ((it * 2f).roundToInt() / 2f).coerceIn(0.5f, 3.0f)
                            crossfadeDurationS = stepped
                        },
                        onValueChangeFinished = {
                            prefs.edit().putFloat("crossfade_duration_s", crossfadeDurationS).apply()
                        },
                        valueRange = 0.5f..3.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0.5s",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = VoxTheme.colors.subtleText
                        )
                        Text(
                            text = "3.0s",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // === 2. APPEARANCE CATEGORY ===
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = VoxTheme.colors.subtleText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Advanced Customization & Theme Studio Entry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, adcuzActivity::class.java)
                        context.startActivity(intent)
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Sliders,
                        contentDescription = "Theme Studio",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Theme Studio & Layout Customizer",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Kustomisasi palet warna, dimensi, offset, background kustom, dan icon pack.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }

                Icon(
                    imageVector = Lucide.ChevronRight,
                    contentDescription = "Open",
                    tint = VoxTheme.colors.subtleText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Ambient Background Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newVal = !dynamicBgEnabled
                        dynamicBgEnabled = newVal
                        prefs.edit().putBoolean("dynamic_background_enabled", newVal).apply()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Dynamic Ambient Background",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tampilkan aura warna album artwork yang lembut di latar belakang PlayerScreen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                Switch(
                    checked = dynamicBgEnabled,
                    onCheckedChange = {
                        dynamicBgEnabled = it
                        prefs.edit().putBoolean("dynamic_background_enabled", it).apply()
                    }
                )
            }

            // Dynamic Background Intensity Slider (10% - 100%)
            if (dynamicBgEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                ) {
                    val intensityPercent = (dynamicBgIntensity * 100f).roundToInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Background Intensity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$intensityPercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    VoxSlider(
                        value = dynamicBgIntensity,
                        onValueChange = {
                            dynamicBgIntensity = it.coerceIn(0.10f, 1.0f)
                        },
                        onValueChangeFinished = {
                            prefs.edit().putFloat("dynamic_background_intensity", dynamicBgIntensity).apply()
                        },
                        valueRange = 0.10f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "10%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = VoxTheme.colors.subtleText
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Motion Blur Toggle
            val isRenderEffectSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isRenderEffectSupported) {
                        val newVal = !motionBlurEnabled
                        motionBlurEnabled = newVal
                        prefs.edit().putBoolean("motion_blur_enabled", newVal).apply()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Motion Blur",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isRenderEffectSupported) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText
                    )
                    Text(
                        text = if (isRenderEffectSupported) {
                            "Terapkan efek blur dinamis berbasis kecepatan gerak pada swipe artwork dan transisi UI."
                        } else {
                            "Tidak didukung pada perangkat ini (Memerlukan Android 12+ / API 31+)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                if (isRenderEffectSupported) {
                    Switch(
                        checked = motionBlurEnabled,
                        onCheckedChange = {
                            motionBlurEnabled = it
                            prefs.edit().putBoolean("motion_blur_enabled", it).apply()
                        }
                    )
                }
            }

            // Motion Blur Intensity Custom Slider (1.0 - 10.0)
            if (isRenderEffectSupported && motionBlurEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blur Intensity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "%.1f".format(motionBlurIntensity),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    VoxSlider(
                        value = motionBlurIntensity,
                        onValueChange = {
                            motionBlurIntensity = it.coerceIn(1.0f, 10.0f)
                        },
                        onValueChangeFinished = {
                            prefs.edit().putFloat("motion_blur_intensity", motionBlurIntensity).apply()
                        },
                        valueRange = 1.0f..10.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Low (1.0)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = VoxTheme.colors.subtleText
                        )
                        Text(
                            text = "High (10.0)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = VoxTheme.colors.subtleText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Animation Curve Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEasingDialog = true }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Animation Curve (Easing)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Karakteristik kurva percepatan transisi UI (${selectedEasing.displayName}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, VoxTheme.colors.divider, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = selectedEasing.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Lucide.ChevronRight,
                        contentDescription = "Select Curve",
                        tint = VoxTheme.colors.subtleText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Animation Duration Slider (100ms - 1000ms, step 50ms)
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Animation Duration",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${animationDurationMs}ms",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                VoxSlider(
                    value = animationDurationMs.toFloat(),
                    onValueChange = {
                        val stepped = ((it / 50f).roundToInt() * 50).coerceIn(100, 1000)
                        animationDurationMs = stepped
                    },
                    onValueChangeFinished = {
                        prefs.edit().putInt("animation_duration_ms", animationDurationMs).apply()
                    },
                    valueRange = 100f..1000f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "100ms",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = VoxTheme.colors.subtleText
                    )
                    Text(
                        text = "1000ms",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = VoxTheme.colors.subtleText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // === 3. LOCKSCREEN CATEGORY ===
            Text(
                text = "LOCKSCREEN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = VoxTheme.colors.subtleText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lockscreen Player Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newVal = !lockscreenPlayerEnabled
                        lockscreenPlayerEnabled = newVal
                        prefs.edit().putBoolean("lockscreen_player_enabled", newVal).apply()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Kontrol Musik di Layar Kunci",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tampilkan kontrol pemutar musik lite saat layar dinyalakan tanpa membuka kunci perangkat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTheme.colors.subtleText
                    )
                }

                Switch(
                    checked = lockscreenPlayerEnabled,
                    onCheckedChange = {
                        lockscreenPlayerEnabled = it
                        prefs.edit().putBoolean("lockscreen_player_enabled", it).apply()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Animation Curve Selector Dialog
    if (showEasingDialog) {
        Dialog(onDismissRequest = { showEasingDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, VoxTheme.colors.divider, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "SELECT ANIMATION CURVE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    VoxEasingCurve.values().forEach { curve ->
                        val isSelected = curve == selectedEasing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedEasing = curve
                                    prefs.edit().putString("animation_easing_curve", curve.name).apply()
                                    showEasingDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = curve.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else VoxTheme.colors.subtleText
                                )
                                Text(
                                    text = curve.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VoxTheme.colors.subtleText,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Lucide.Check,
                                    contentDescription = "Selected",
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
