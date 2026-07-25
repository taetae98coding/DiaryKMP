package io.github.taetae98coding.diary.compose.core.pulltorefresh

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun PullToRefreshGestureBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val scrollableState = rememberScrollableState { 0F }

    Box(
        modifier =
            modifier.scrollable(
                state = scrollableState,
                orientation = Orientation.Vertical,
            ),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun PullToRefreshGestureBoxPreview() {
    DiaryTheme {
        PullToRefreshGestureBox {
            Text(text = "스크롤 제스처 영역")
        }
    }
}
