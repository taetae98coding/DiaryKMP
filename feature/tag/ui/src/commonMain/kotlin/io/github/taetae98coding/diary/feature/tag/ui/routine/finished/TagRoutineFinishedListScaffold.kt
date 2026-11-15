package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

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
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.FinishedRoutineList
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_routine_finished_list_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_routine_finished_list_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_routine_finished_list_subtitle
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagRoutineFinishedListScaffold(
    onEvent: (TagRoutineFinishedListScaffoldEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: RoutineListState = rememberRoutineListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    uiStateProvider: () -> TagRoutineFinishedListUiState = { TagRoutineFinishedListUiState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        FinishedRoutineList(
            onEvent = onRoutineListEvent,
            onRefresh = { onEvent(TagRoutineFinishedListScaffoldEvent.Refresh) },
            emptyTitle = stringResource(Res.string.tag_routine_finished_list_empty_title),
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

@Composable
private fun TopBar(
    onEvent: (TagRoutineFinishedListScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagRoutineFinishedListUiState = { TagRoutineFinishedListUiState() },
) {
    val subtitle = stringResource(Res.string.tag_routine_finished_list_subtitle)
    val navigateUpContentDescription = stringResource(Res.string.tag_routine_finished_list_navigate_up_button_content_description)

    DiaryTopBar(
        title = {
            Title(
                text = uiStateProvider().title,
                subtitle = subtitle,
                isMarquee = true,
            )
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(TagRoutineFinishedListScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
    )
}

@ScreenPreview
@Composable
private fun TagRoutineFinishedListScaffoldPreview() {
    DiaryTheme {
        TagRoutineFinishedListScaffold(
            onEvent = {},
            onRoutineListEvent = {},
            uiStateProvider = { TagRoutineFinishedListUiState(title = "📌 Tag") },
        )
    }
}
