package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PullToRefreshGestureBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryRefreshableEmptyBox(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
    content: @Composable BoxScope.() -> Unit,
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        PullToRefreshGestureBox(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryRefreshableEmptyBoxPreview() {
    DiaryTheme {
        DiaryRefreshableEmptyBox(onRefresh = {}) {
            DiaryEmptyBox(
                title = "아직 메모가 없습니다",
                description = "추가 버튼으로 새 메모를 만들 수 있습니다",
                icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}
