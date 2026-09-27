package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoList
import io.github.taetae98coding.diary.feature.search.ui.previewMemo
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun SearchHomeResultPager(
    modifier: Modifier = Modifier,
    state: SearchHomeScaffoldState = rememberSearchHomeScaffoldState(),
    resultContent: @Composable (SearchHomeType) -> Unit,
) {
    HorizontalPager(
        state = state.pagerState,
        modifier = modifier,
    ) { page ->
        resultContent(searchHomeTypeList[page])
    }
}

@ScreenPreview
@Composable
private fun SearchHomeResultPagerPreview() {
    val memoPagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewMemo(title = "여름 여행 계획", color = 0xFF3A7BD5),
                        previewMemo(title = "여행 준비물", color = 0xFFE57373),
                    ),
                ),
            )
        }
    val memoPagingItems = memoPagingData.collectAsLazyPagingItems()

    DiaryTheme {
        SearchHomeResultPager(modifier = Modifier.fillMaxSize()) {
            SearchHomeMemoList(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
                memoPagingItems = memoPagingItems,
            )
        }
    }
}
