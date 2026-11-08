package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.DDayListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.dday.UpdateDDayListTodayEffect
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun DDayHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
    componentVisibleProvider: () -> DDayHomeScaffoldComponentVisible,
    ddayViewModel: DDayHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberDDayListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val ddayPagingItems = ddayViewModel.ddayPagingData.collectAsLazyPagingItems()
    val filterUiState by ddayViewModel.filterUiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    UpdateDDayListTodayEffect(state = listState)
    DDayListUndoSnackbarEffect(
        onRestore = ddayViewModel::restore,
        effect = ddayViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    DDayHomeScaffold(
        onEvent = { event ->
            when (event) {
                is DDayHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is DDayHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is DDayHomeScaffoldEvent.ClickFilter -> navigateToFilter()
                is DDayHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onDDayListEvent = { event ->
            when (event) {
                is DDayListEvent.ClickDDay -> navigateToDetail(event.id)
                is DDayListEvent.SwipeDelete -> ddayViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        listState = listState,
        snackbarHostState = snackbarHostState,
        ddayPagingItems = ddayPagingItems,
        filterUiStateProvider = { filterUiState },
        syncUiStateProvider = { syncUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
