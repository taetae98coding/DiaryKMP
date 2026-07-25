package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor

@Stable
public class DiaryColorInputState(
    initialColor: Color,
) {
    private val animatable =
        Animatable(
            initialValue = initialColor,
            typeConverter = Color.VectorConverter(initialColor.colorSpace),
        )

    public val color: Color
        get() = animatable.value

    public suspend fun animateTo(color: Color) {
        animatable.animateTo(targetValue = color)
    }

    public companion object {
        internal val Saver =
            Saver<DiaryColorInputState, Int>(
                save = { it.color.toArgb() },
                restore = { DiaryColorInputState(initialColor = Color(color = it)) },
            )
    }
}

@Composable
public fun rememberDiaryColorInputState(initialColor: Color = randomColor()): DiaryColorInputState =
    rememberSaveable(saver = DiaryColorInputState.Saver) {
        DiaryColorInputState(initialColor = initialColor)
    }
