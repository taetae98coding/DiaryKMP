package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.animation.DiaryValueSlide

@Composable
internal fun <T : Comparable<T>> AnimatedValueText(
    value: T,
    text: @Composable (T) -> String,
) {
    DiaryValueSlide(targetState = value) { targetValue ->
        Text(text = text(targetValue))
    }
}
