package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public const val DIARY_REFRESHABLE_STAGGERED_GRID_TEST_TAG: String = "DiaryRefreshableStaggeredGrid"

private const val COLUMN_COUNT = 2

@Composable
public fun DiaryRefreshableStaggeredGrid(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    isRefreshingProvider: () -> Boolean = { false },
    listTestTag: String = DIARY_REFRESHABLE_STAGGERED_GRID_TEST_TAG,
    content: LazyStaggeredGridScope.() -> Unit,
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(COLUMN_COUNT),
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(listTestTag),
            state = state,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalItemSpacing = DiaryTheme.dimens.itemSpacing,
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
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
