package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.FinishedRoutineList
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.previewRoutineSummary
import io.github.taetae98coding.diary.compose.routine.previewRoutineToday
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_finished_list_empty_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_finished_list_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_navigate_up_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineFinishedListScaffold(
    onEvent: (RoutineFinishedListScaffoldEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: RoutineListState = rememberRoutineListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.routine_finished_list_title),
                onNavigateUp = { onEvent(RoutineFinishedListScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.routine_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        FinishedRoutineList(
            onEvent = onRoutineListEvent,
            onRefresh = { onEvent(RoutineFinishedListScaffoldEvent.Refresh) },
            emptyTitle = stringResource(Res.string.routine_finished_list_empty_title),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = listState,
            gridState = gridState,
            routinePagingItems = routinePagingItems,
            isRefreshingProvider = { syncUiStateProvider().isRefreshing },
        )
    }
}

@ScreenPreview
@Composable
private fun RoutineFinishedListScaffoldPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewRoutineSummary(title = "물 마시기", isFinished = true)))) }

    DiaryTheme {
        RoutineFinishedListScaffold(
            onEvent = {},
            onRoutineListEvent = {},
            listState = rememberRoutineListState(initialToday = previewRoutineToday()),
            routinePagingItems = pagingData.collectAsLazyPagingItems(),
        )
    }
}
