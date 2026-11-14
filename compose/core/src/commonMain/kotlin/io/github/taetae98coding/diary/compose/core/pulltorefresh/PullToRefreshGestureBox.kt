package io.github.taetae98coding.diary.compose.core.pulltorefresh

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun PullToRefreshGestureBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit = {},
) {
    // 당김 새로고침은 중첩 스크롤로만 시작되므로, 스크롤할 내용이 없는 영역에는 아무것도 소비하지 않는 scrollable을 둔다.
    // verticalScroll과 달리 자식의 높이 제약을 무한으로 바꾸지 않아 자식이 영역을 채우고 가운데에 놓일 수 있다.
    val scrollableState = rememberScrollableState { 0F }

    Box(
        modifier =
            modifier.scrollable(
                state = scrollableState,
                orientation = Orientation.Vertical,
            ),
        contentAlignment = contentAlignment,
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
