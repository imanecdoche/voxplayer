package com.vox.music.feature.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vox.music.feature.library.TrackSortOrder

@Composable
fun AlphabetIndexerBar(
    sortOrder: TrackSortOrder,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sortOrder != TrackSortOrder.TITLE_ASC && sortOrder != TrackSortOrder.TITLE_DESC) {
        return
    }

    val alphabet = remember(sortOrder) {
        if (sortOrder == TrackSortOrder.TITLE_ASC) {
            listOf('#') + ('A'..'Z').toList()
        } else {
            ('Z' downTo 'A').toList() + listOf('#')
        }
    }

    val haptic = LocalHapticFeedback.current
    var barHeightPx by remember { mutableFloatStateOf(1f) }
    var isTouching by remember { mutableStateOf(false) }
    var activeIndex by remember { mutableIntStateOf(-1) }

    fun handleTouch(y: Float) {
        if (barHeightPx <= 0f || alphabet.isEmpty()) return
        val clampedY = y.coerceIn(0f, barHeightPx)
        val index = ((clampedY / barHeightPx) * alphabet.size).toInt().coerceIn(0, alphabet.size - 1)
        if (index != activeIndex) {
            activeIndex = index
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onLetterSelected(alphabet[index])
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 4.dp, top = 16.dp, bottom = 96.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 1. Alphabet Column
        Column(
            modifier = Modifier
                .width(22.dp)
                .fillMaxHeight()
                .onGloballyPositioned { coordinates ->
                    barHeightPx = coordinates.size.height.toFloat()
                }
                .pointerInput(alphabet) {
                    detectTapGestures(
                        onPress = { offset ->
                            isTouching = true
                            handleTouch(offset.y)
                            tryAwaitRelease()
                            isTouching = false
                            activeIndex = -1
                        }
                    )
                }
                .pointerInput(alphabet) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isTouching = true
                            handleTouch(offset.y)
                        },
                        onDragEnd = {
                            isTouching = false
                            activeIndex = -1
                        },
                        onDragCancel = {
                            isTouching = false
                            activeIndex = -1
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            handleTouch(change.position.y)
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            alphabet.forEachIndexed { index, char ->
                val isCharActive = isTouching && index == activeIndex
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = if (isCharActive) 11.sp else 8.5.sp,
                        fontWeight = if (isCharActive) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = if (isCharActive) MaterialTheme.colorScheme.onBackground else Color(0xFF757575)
                )
            }
        }

        // 2. Floating Alphabet Indicator Popup
        AnimatedVisibility(
            visible = isTouching && activeIndex in alphabet.indices,
            enter = fadeIn(tween(100)) + scaleIn(tween(100)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-48).dp)
        ) {
            if (activeIndex in alphabet.indices) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = alphabet[activeIndex].toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
