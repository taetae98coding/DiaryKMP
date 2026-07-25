package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SliderState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal const val CHANNEL_MAX = 255
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL_MASK = 0xFF

@Stable
public class ColorPickerState internal constructor(
    internal val redSliderState: SliderState,
    internal val greenSliderState: SliderState,
    internal val blueSliderState: SliderState,
    internal val hexTextFieldState: TextFieldState,
) {
    public val color: Color
        get() =
            Color(
                red = redSliderState.value.roundToInt(),
                green = greenSliderState.value.roundToInt(),
                blue = blueSliderState.value.roundToInt(),
            )

    internal var isAnimating: Boolean = false
        private set

    internal var targetColor: Color? = null
        private set

    private val animationMutatorMutex = MutatorMutex()

    internal suspend fun animateColorTo(value: Color) {
        animationMutatorMutex.mutate {
            isAnimating = true
            targetColor = value

            try {
                val argb = value.toArgb()

                coroutineScope {
                    launch { redSliderState.animateTo(target = (argb shr RED_SHIFT and CHANNEL_MASK).toFloat()) }
                    launch { greenSliderState.animateTo(target = (argb shr GREEN_SHIFT and CHANNEL_MASK).toFloat()) }
                    launch { blueSliderState.animateTo(target = (argb and CHANNEL_MASK).toFloat()) }
                }
            } finally {
                isAnimating = false
                targetColor = null
            }
        }
    }
}

private suspend fun SliderState.animateTo(target: Float) {
    animate(initialValue = value, targetValue = target) { current, _ ->
        value = current
    }
}

@Composable
public fun rememberColorPickerState(initialColor: Color): ColorPickerState {
    val argb = initialColor.toArgb()
    val redSliderState = rememberSliderState(value = (argb shr RED_SHIFT and CHANNEL_MASK).toFloat(), valueRange = 0F..CHANNEL_MAX.toFloat())
    val greenSliderState = rememberSliderState(value = (argb shr GREEN_SHIFT and CHANNEL_MASK).toFloat(), valueRange = 0F..CHANNEL_MAX.toFloat())
    val blueSliderState = rememberSliderState(value = (argb and CHANNEL_MASK).toFloat(), valueRange = 0F..CHANNEL_MAX.toFloat())
    val hexTextFieldState = rememberTextFieldState(initialText = initialColor.toHexString())

    return remember(redSliderState, greenSliderState, blueSliderState, hexTextFieldState) {
        ColorPickerState(
            redSliderState = redSliderState,
            greenSliderState = greenSliderState,
            blueSliderState = blueSliderState,
            hexTextFieldState = hexTextFieldState,
        )
    }
}
