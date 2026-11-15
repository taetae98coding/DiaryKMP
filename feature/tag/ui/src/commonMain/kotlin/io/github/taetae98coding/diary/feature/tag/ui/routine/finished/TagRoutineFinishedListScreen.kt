package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.routine.UpdateRoutineListTodayEffect
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun TagRoutineFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToRoutineDetail: (Uuid) -> Unit,
    routineViewModel: TagRoutineFinishedListViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberRoutineListState()
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by routineViewModel.uiState.collectAsStateWithLifecycle()
    val routinePagingItems = routineViewModel.routinePagingData.collectAsLazyPagingItems()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    UpdateRoutineListTodayEffect(state = listState)
    UpdateTagRoutineFinishedListTodayEffect(
        routineViewModel = routineViewModel,
        listState = listState,
    )
    RoutineListUndoSnackbarEffect(
        onUndo = { effect ->
            when (effect) {
                is RoutineListEffect.Finished -> routineViewModel.restart(id = effect.id)
                is RoutineListEffect.Restarted -> routineViewModel.finish(id = effect.id)
                is RoutineListEffect.Deleted -> routineViewModel.restore(id = effect.id)
            }
        },
        effect = routineViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    TagRoutineFinishedListScaffold(
        onEvent = { event ->
            when (event) {
                is TagRoutineFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is TagRoutineFinishedListScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onRoutineListEvent = { event ->
            when (event) {
                is RoutineListEvent.ClickRoutine -> navigateToRoutineDetail(event.id)
                is RoutineListEvent.SwipeFinish -> routineViewModel.finish(id = event.id)
                is RoutineListEvent.SwipeRestart -> routineViewModel.restart(id = event.id)
                is RoutineListEvent.SwipeDelete -> routineViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        listState = listState,
        gridState = gridState,
        snackbarHostState = snackbarHostState,
        routinePagingItems = routinePagingItems,
        syncUiStateProvider = { syncUiState },
        uiStateProvider = { uiState },
    )
}
