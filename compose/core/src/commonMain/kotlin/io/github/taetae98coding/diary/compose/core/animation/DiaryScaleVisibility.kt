package io.github.taetae98coding.diary.compose.core.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryScaleVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(),
        exit = scaleOut(),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun DiaryScaleVisibilityPreview() {
    DiaryTheme {
        Surface {
            DiaryScaleVisibility(visible = true) {
                Text(text = "표시")
            }
        }
    }
}
