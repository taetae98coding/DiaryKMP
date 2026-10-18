package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryRefreshableStaggeredGrid(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    isRefreshingProvider: () -> Boolean = { false },
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
    content: LazyStaggeredGridScope.() -> Unit,
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(DiaryRefreshableGridDefaults.COLUMN_COUNT),
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = DiaryRefreshableGridDefaults.contentPadding(bottomPadding = bottomPadding),
            verticalItemSpacing = DiaryTheme.dimens.itemSpacing,
            horizontalArrangement = DiaryRefreshableGridDefaults.ItemArrangement,
            content = content,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryRefreshableStaggeredGridPreview() {
    DiaryTheme {
        DiaryRefreshableStaggeredGrid(onRefresh = {}) {
            items(items = List(size = 4) { index -> index }) { index ->
                Card {
                    Text(
                        text = "항목 $index",
                        modifier =
                            Modifier
                                .height(PREVIEW_ITEM_HEIGHT * (index % 2 + 1))
                                .padding(16.dp),
                    )
                }
            }
        }
    }
}

private val PREVIEW_ITEM_HEIGHT = 56.dp
