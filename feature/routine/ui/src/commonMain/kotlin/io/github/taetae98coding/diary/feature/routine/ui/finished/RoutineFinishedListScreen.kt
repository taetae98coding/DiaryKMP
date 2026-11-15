package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.RoutineListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.routine.UpdateRoutineListTodayEffect
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun RoutineFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    routineViewModel: RoutineFinishedListViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberRoutineListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val routinePagingItems = routineViewModel.routinePagingData.collectAsLazyPagingItems()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    UpdateRoutineListTodayEffect(state = listState)
    UpdateRoutineFinishedListTodayEffect(
        routineViewModel = routineViewModel,
        listState = listState,
    )
    RoutineListUndoSnackbarEffect(
        onUndo = routineViewModel::undo,
        effect = routineViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    RoutineFinishedListScaffold(
        onEvent = { event ->
            when (event) {
                is RoutineFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is RoutineFinishedListScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onRoutineListEvent = { event ->
            when (event) {
                is RoutineListEvent.ClickRoutine -> navigateToDetail(event.id)
                is RoutineListEvent.SwipeFinish -> routineViewModel.finish(id = event.id)
                is RoutineListEvent.SwipeRestart -> routineViewModel.restart(id = event.id)
                is RoutineListEvent.SwipeDelete -> routineViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        listState = listState,
        snackbarHostState = snackbarHostState,
        routinePagingItems = routinePagingItems,
        syncUiStateProvider = { syncUiState },
    )
}

@Composable
private fun UpdateRoutineFinishedListTodayEffect(
    routineViewModel: RoutineFinishedListViewModel,
    listState: RoutineListState,
) {
    LaunchedEffect(listState, routineViewModel) {
        snapshotFlow { listState.today }
            .collect { today -> routineViewModel.updateToday(today = today) }
    }
}
