package io.github.taetae98coding.diary.compose.core.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
internal fun <T> DiaryScaleCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
        contentAlignment = Alignment.Center,
    ) { state ->
        content(state)
    }
}

@ComponentPreview
@Composable
private fun DiaryScaleCrossfadePreview() {
    DiaryTheme {
        Surface {
            DiaryScaleCrossfade(targetState = true) { isVisible ->
                Text(text = if (isVisible) "표시" else "숨김")
            }
        }
    }
}
