package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun <T : Any> SearchHomeResultContent(
    uiState: StateFlow<SearchHomeResultUiState>,
    pagingItems: LazyPagingItems<T>,
    listState: LazyListState,
    content: @Composable (state: SearchHomeResultContentState<T>) -> Unit,
) {
    val uiStateValue by uiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    content(
        SearchHomeResultContentState(
            pagingItems = pagingItems,
            uiState = uiStateValue,
            listState = listState,
            sortSheetState = sortSheetState,
        ),
    )
}
