@file:OptIn(ExperimentalAnimationApi::class)

package io.github.taetae98coding.diary.compose.core.animation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun <T> DiaryCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any? = { it },
    content: @Composable (T) -> Unit,
) {
    updateTransition(targetState = targetState)
        .Crossfade(
            modifier = modifier,
            contentKey = contentKey,
            content = content,
        )
}

@ComponentPreview
@Composable
private fun DiaryCrossfadePreview() {
    DiaryTheme {
        Surface {
            Box {
                DiaryCrossfade(targetState = true) { isVisible ->
                    Text(text = if (isVisible) "표시" else "숨김")
                }
            }
        }
    }
}
