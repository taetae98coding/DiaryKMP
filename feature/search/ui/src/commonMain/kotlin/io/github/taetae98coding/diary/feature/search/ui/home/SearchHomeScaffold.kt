package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoList
import io.github.taetae98coding.diary.feature.search.ui.previewMemo
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun SearchHomeScaffold(
    onEvent: (SearchHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SearchHomeScaffoldState = rememberSearchHomeScaffoldState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    resultContent: @Composable (SearchHomeType) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
        topBar = {
            SearchHomeTopBar(
                onEvent = onEvent,
                state = state,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            SearchHomeTabRow(state = state)
            SearchHomeResultPager(
                modifier = Modifier.weight(1f),
                state = state,
                resultContent = resultContent,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomeScaffoldPreview() {
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
        SearchHomeScaffold(onEvent = {}) {
            SearchHomeMemoList(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
                memoPagingItems = memoPagingItems,
            )
        }
    }
}
