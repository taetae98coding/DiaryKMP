package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public const val DIARY_REFRESHABLE_GRID_TEST_TAG: String = "DiaryRefreshableGrid"

private const val COLUMN_COUNT = 2

@Composable
public fun DiaryRefreshableGrid(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    isRefreshingProvider: () -> Boolean = { false },
    listTestTag: String = DIARY_REFRESHABLE_GRID_TEST_TAG,
    content: LazyGridScope.() -> Unit,
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(COLUMN_COUNT),
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(listTestTag),
            state = state,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            content = content,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryRefreshableGridPreview() {
    DiaryTheme {
        DiaryRefreshableGrid(onRefresh = {}) {
            items(items = List(size = 4) { index -> "항목 ${'$'}index" }) { item ->
                Card {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
