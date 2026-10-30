package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel

@Composable
internal fun <T : Any> SearchHomeResultContent(
    viewModel: SearchHomeResultViewModel<T>,
    pagingItems: LazyPagingItems<T>,
    listState: LazyListState,
    content: @Composable (state: SearchHomeResultContentState<T>) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    content(
        SearchHomeResultContentState(
            pagingItems = pagingItems,
            uiState = uiState,
            listState = listState,
            sortSheetState = sortSheetState,
        ),
    )
}
