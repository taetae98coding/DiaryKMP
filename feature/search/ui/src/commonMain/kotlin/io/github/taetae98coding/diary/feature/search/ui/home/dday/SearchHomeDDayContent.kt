package io.github.taetae98coding.diary.feature.search.ui.home.dday

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.DDayListUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultContent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultItemEvent
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeDDayContent(
    viewModel: SearchHomeDDayViewModel,
    pagingItems: LazyPagingItems<DDay>,
    listState: LazyListState,
    ddayListState: DDayListState,
    snackbarHostState: SnackbarHostState,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchHomeResultContent(
        uiState = viewModel.uiState,
        pagingItems = pagingItems,
        listState = listState,
    ) { state ->
        DDayListUndoSnackbarEffect(
            onRestore = viewModel::restore,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeDDayList(
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
            ddayListState = ddayListState,
            sortSheetState = state.sortSheetState,
            ddayPagingItems = state.pagingItems,
            query = state.uiState.appliedQuery,
            sortProvider = { state.uiState.sort },
        )
    }
}
