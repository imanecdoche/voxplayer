package com.vox.music.feature.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.core.theme.model.toComposeColor
import com.vox.music.core.theme.model.toHex
import com.vox.music.ui.components.VoxSlider
import com.vox.music.ui.theme.VoxTheme

@Composable
fun ColorPickerDialog(
    title: String,
    initialHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialColor = remember(initialHex) { initialHex.toComposeColor(Color.White) }
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }

    var hexInput by remember { mutableStateOf(initialHex.uppercase()) }

    val currentColor = Color(red, green, blue, alpha)

    // Common palette presets
    val presets = listOf(
        "#FFFFFF", "#000000", "#757575", "#222222",
        "#FF5252", "#FF4081", "#E040FB", "#7C4DFF",
        "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
        "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41",
        "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF18181A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Color Preview Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentColor)
                            .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            hexInput = input.uppercase()
                            if (input.startsWith("#") && (input.length == 7 || input.length == 9)) {
                                val parsed = input.toComposeColor(currentColor)
                                red = parsed.red
                                green = parsed.green
                                blue = parsed.blue
                                alpha = parsed.alpha
                            }
                        },
                        label = { Text("HEX CODE", color = Color(0xFF8E8E93), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF3A3A3C)
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RGB & Alpha Sliders
                ColorSliderRow(label = "R", value = red, onValueChange = {
                    red = it
                    hexInput = Color(red, green, blue, alpha).toHex(alpha < 0.99f)
                }, barColor = Color.Red)

                ColorSliderRow(label = "G", value = green, onValueChange = {
                    green = it
                    hexInput = Color(red, green, blue, alpha).toHex(alpha < 0.99f)
                }, barColor = Color.Green)

                ColorSliderRow(label = "B", value = blue, onValueChange = {
                    blue = it
                    hexInput = Color(red, green, blue, alpha).toHex(alpha < 0.99f)
                }, barColor = Color.Blue)

                ColorSliderRow(label = "A", value = alpha, onValueChange = {
                    alpha = it
                    hexInput = Color(red, green, blue, alpha).toHex(alpha < 0.99f)
                }, barColor = Color.White)

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Palette Presets
                Text(
                    text = "PRESET PALETTES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(7).forEach { hex ->
                        val presetColor = hex.toComposeColor()
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(presetColor)
                                .border(1.dp, Color(0xFF3A3A3C), CircleShape)
                                .clickable {
                                    red = presetColor.red
                                    green = presetColor.green
                                    blue = presetColor.blue
                                    alpha = presetColor.alpha
                                    hexInput = hex
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHex = Color(red, green, blue, alpha).toHex(alpha < 0.99f)
                    onColorSelected(finalHex)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF8E8E93))
            }
        }
    )
}

@Composable
private fun ColorSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    barColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = barColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(20.dp)
        )

        VoxSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = (value * 255).toInt().toString(),
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier
                .width(34.dp)
                .padding(start = 6.dp)
        )
    }
}
