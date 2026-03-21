package dev.achmad.fsync.ui.components

/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications made by w2sv on 12.06.2024
 */

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Preview
@Composable
private fun Prev() {
    MaterialTheme {
        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            UnpaddedSwitch(checked = true, onCheckedChange = {})
        }
    }
}

@Composable
fun UnpaddedSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val switchWidth = SwitchWidth * scale
    val switchHeight = SwitchHeight * scale
    val thumbDiameter = ThumbDiameter * scale
    val uncheckedThumbDiameter = if (thumbContent == null) UncheckedThumbDiameter * scale else thumbDiameter
    val thumbPadding = (switchHeight - thumbDiameter) / 2
    val thumbPathLength = (switchWidth - thumbDiameter) - thumbPadding
    val pressedHandleWidth = PressedHandleWidth * scale
    val trackOutlineWidth = TrackOutlineWidth * scale
    val stateLayerSize = StateLayerSize * scale

    val thumbPaddingStart = (switchHeight - uncheckedThumbDiameter) / 2
    val minBound = with(LocalDensity.current) { thumbPaddingStart.toPx() }
    val maxBound = with(LocalDensity.current) { thumbPathLength.toPx() }
    val valueToOffset = remember<(Boolean) -> Float>(minBound, maxBound) {
        { value -> if (value) maxBound else minBound }
    }

    val targetValue = valueToOffset(checked)
    val offset = remember { Animatable(targetValue) }
    val scope = rememberCoroutineScope()

    SideEffect {
        // min bound might have changed if the icon is only rendered in checked state.
        offset.updateBounds(lowerBound = minBound)
    }

    DisposableEffect(checked) {
        if (offset.targetValue != targetValue) {
            scope.launch {
                offset.animateTo(targetValue, AnimationSpec)
            }
        }
        onDispose { }
    }

    // TODO: Add Swipeable modifier b/223797571
    val toggleableModifier =
        if (onCheckedChange != null) {
            Modifier.toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null
            )
        } else {
            Modifier
        }

    Box(
        modifier
            .then(toggleableModifier)
            .wrapContentSize(Alignment.Center)
            .requiredSize(switchWidth, switchHeight)
    ) {
        SwitchImpl(
            checked = checked,
            enabled = enabled,
            colors = colors,
            thumbValue = offset.asState(),
            interactionSource = interactionSource,
            thumbShape = ThumbShape,
            uncheckedThumbDiameter = uncheckedThumbDiameter,
            thumbDiameter = thumbDiameter,
            minBound = thumbPaddingStart,
            maxBound = thumbPathLength,
            thumbContent = thumbContent,
            switchWidth = switchWidth,
            switchHeight = switchHeight,
            pressedHandleWidth = pressedHandleWidth,
            trackOutlineWidth = trackOutlineWidth,
            stateLayerSize = stateLayerSize,
        )
    }
}

@Composable
@Suppress("ComposableLambdaParameterNaming", "ComposableLambdaParameterPosition")
private fun BoxScope.SwitchImpl(
    checked: Boolean,
    enabled: Boolean,
    colors: SwitchColors,
    thumbValue: State<Float>,
    thumbContent: (@Composable () -> Unit)?,
    interactionSource: InteractionSource,
    thumbShape: Shape,
    uncheckedThumbDiameter: Dp,
    thumbDiameter: Dp,
    minBound: Dp,
    maxBound: Dp,
    switchWidth: Dp,
    switchHeight: Dp,
    pressedHandleWidth: Dp,
    trackOutlineWidth: Dp,
    stateLayerSize: Dp,
) {
    val trackColor = colors.trackColor(enabled, checked)
    val isPressed by interactionSource.collectIsPressedAsState()

    val thumbValueDp = with(LocalDensity.current) { thumbValue.value.toDp() }
    val thumbSizeDp = if (isPressed) {
        pressedHandleWidth
    } else {
        uncheckedThumbDiameter + (thumbDiameter - uncheckedThumbDiameter) *
                ((thumbValueDp - minBound) / (maxBound - minBound))
    }

    val thumbOffset = if (isPressed) {
        with(LocalDensity.current) {
            if (checked) {
                maxBound - trackOutlineWidth
            } else {
                trackOutlineWidth
            }.toPx()
        }
    } else {
        thumbValue.value
    }

    val trackShape = ThumbShape
    val modifier = Modifier
        .align(Alignment.Center)
        .width(switchWidth)
        .height(switchHeight)
        .border(
            trackOutlineWidth,
            colors.borderColor(enabled, checked),
            trackShape
        )
        .background(trackColor, trackShape)

    Box(modifier) {
        val resolvedThumbColor = colors.thumbColor(enabled, checked)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = false,
                        stateLayerSize / 2
                    )
                )
                .requiredSize(thumbSizeDp)
                .background(resolvedThumbColor, thumbShape),
            contentAlignment = Alignment.Center
        ) {
            if (thumbContent != null) {
                val iconColor = colors.iconColor(enabled, checked)
                CompositionLocalProvider(
                    LocalContentColor provides iconColor,
                    content = thumbContent
                )
            }
        }
    }
}

private val ThumbShape = RoundedCornerShape(32.dp)
private val TrackOutlineWidth = 2.0.dp
private val TrackWidth = 52.0.dp  // 52.0.dp
private val TrackHeight = 32.0.dp  // 32.0.dp
private val StateLayerSize = 40.0.dp
private val SelectedHandleWidth = 24.0.dp  // 24.0.dp
private val UnselectedHandleWidth = 16.0.dp  // 16.0.dp
internal val ThumbDiameter = SelectedHandleWidth
internal val UncheckedThumbDiameter = UnselectedHandleWidth
private val SwitchWidth = TrackWidth
private val SwitchHeight = TrackHeight
private val ThumbPadding = (SwitchHeight - ThumbDiameter) / 2
private val ThumbPathLength = (SwitchWidth - ThumbDiameter) - ThumbPadding
private val PressedHandleWidth = 28.0.dp

private val AnimationSpec = TweenSpec<Float>(durationMillis = 100)

@Stable
internal fun SwitchColors.thumbColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedThumbColor else uncheckedThumbColor
    } else {
        if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
    }

/**
 * Represents the color used for the switch's track, depending on [enabled] and [checked].
 *
 * @param enabled whether the [Switch] is enabled or not
 * @param checked whether the [Switch] is checked or not
 */
@Stable
internal fun SwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackColor else uncheckedTrackColor
    } else {
        if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
    }

/**
 * Represents the color used for the switch's border, depending on [enabled] and [checked].
 *
 * @param enabled whether the [Switch] is enabled or not
 * @param checked whether the [Switch] is checked or not
 */
@Stable
internal fun SwitchColors.borderColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedBorderColor else uncheckedBorderColor
    } else {
        if (checked) disabledCheckedBorderColor else disabledUncheckedBorderColor
    }

/**
 * Represents the content color passed to the icon if used
 *
 * @param enabled whether the [Switch] is enabled or not
 * @param checked whether the [Switch] is checked or not
 */
@Stable
internal fun SwitchColors.iconColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedIconColor else uncheckedIconColor
    } else {
        if (checked) disabledCheckedIconColor else disabledUncheckedIconColor
    }