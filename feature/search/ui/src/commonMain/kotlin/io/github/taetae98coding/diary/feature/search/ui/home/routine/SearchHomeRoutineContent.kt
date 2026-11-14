package io.github.taetae98coding.diary.feature.search.ui.home.routine

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.RoutineListUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultContent
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeRoutineContent(
    viewModel: SearchHomeRoutineViewModel,
    pagingItems: LazyPagingItems<RoutineSummary>,
    listState: LazyListState,
    routineListState: RoutineListState,
    snackbarHostState: SnackbarHostState,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchHomeResultContent(
        uiState = viewModel.uiState,
        pagingItems = pagingItems,
        listState = listState,
    ) { state ->
        RoutineListUndoSnackbarEffect(
            onUndo = viewModel::undo,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeRoutineList(
            onRoutineListEvent = { event ->
                when (event) {
                    is RoutineListEvent.ClickRoutine -> navigateToDetail(event.id)
                    is RoutineListEvent.SwipeFinish -> viewModel.finish(id = event.id)
                    is RoutineListEvent.SwipeRestart -> viewModel.restart(id = event.id)
                    is RoutineListEvent.SwipeDelete -> viewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            listState = state.listState,
            routineListState = routineListState,
            routinePagingItems = state.pagingItems,
            query = state.uiState.appliedQuery,
        )
    }
}
