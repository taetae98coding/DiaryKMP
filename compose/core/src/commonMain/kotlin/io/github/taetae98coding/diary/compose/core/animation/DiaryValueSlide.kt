package io.github.taetae98coding.diary.compose.core.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun <T : Comparable<T>> DiaryValueSlide(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { height -> height } togetherWith
                    slideOutVertically { height -> -height }
            } else {
                slideInVertically { height -> -height } togetherWith
                    slideOutVertically { height -> height }
            }
        },
    ) { value ->
        content(value)
    }
}

@ComponentPreview
@Composable
private fun DiaryValueSlidePreview() {
    DiaryTheme {
        Surface {
            DiaryValueSlide(targetState = 1) { value ->
                Text(text = value.toString())
            }
        }
    }
}
