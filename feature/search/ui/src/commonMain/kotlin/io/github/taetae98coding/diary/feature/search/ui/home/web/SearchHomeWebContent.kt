package io.github.taetae98coding.diary.feature.search.ui.home.web

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.web.WebListUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultContent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultItemEvent
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeWebContent(
    viewModel: SearchHomeWebViewModel,
    pagingItems: LazyPagingItems<Web>,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchHomeResultContent(
        viewModel = viewModel,
        pagingItems = pagingItems,
        listState = listState,
    ) { state ->
        WebListUndoSnackbarEffect(
            onRestore = viewModel::restore,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeWebList(
            onEvent = { event ->
                when (event) {
                    is SearchHomeResultEvent.ClickSort -> state.sortSheetState.show()
                    is SearchHomeResultEvent.SelectSort -> viewModel.select(sort = event.sort)
                }
            },
            onItemEvent = { event ->
                when (event) {
                    is SearchHomeResultItemEvent.Click -> navigateToDetail(event.id)
                    is SearchHomeResultItemEvent.SwipeDelete -> viewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            listState = state.listState,
            sortSheetState = state.sortSheetState,
            webPagingItems = state.pagingItems,
            query = state.uiState.appliedQuery,
            sortProvider = { state.uiState.sort },
        )
    }
}
