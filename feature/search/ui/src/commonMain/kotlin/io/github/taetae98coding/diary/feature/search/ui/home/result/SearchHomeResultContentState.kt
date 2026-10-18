package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.LazyListState
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultUiState

internal class SearchHomeResultContentState<T : Any>(
    val pagingItems: LazyPagingItems<T>,
    val uiState: SearchHomeResultUiState,
    val listState: LazyListState,
    val sortSheetState: DialogState,
)
