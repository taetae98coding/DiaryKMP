package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.compose.tag.list.TagListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun TagFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    tagViewModel: TagFinishedListViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val sortUiState by tagViewModel.sortUiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    TagListUndoSnackbarEffect(
        onRestart = { id -> tagViewModel.restart(id = id) },
        onFinish = { id -> tagViewModel.finish(id = id) },
        onRestore = { id -> tagViewModel.restore(id = id) },
        effect = tagViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    TagFinishedListScaffold(
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        tagPagingItems = tagPagingItems,
        onEvent = { event ->
            when (event) {
                is TagFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is TagFinishedListScaffoldEvent.ClickSort -> sortSheetState.show()
                is TagFinishedListScaffoldEvent.SelectSort -> tagViewModel.select(sort = event.sort)
                is TagFinishedListScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onTagListEvent = { event ->
            handleTagListEvent(event = event, navigateToDetail = navigateToDetail, tagViewModel = tagViewModel)
        },
        modifier = modifier,
        uiStateProvider = { uiState },
        sortProvider = { sortUiState.sort },
    )
}

private fun handleTagListEvent(
    event: TagListEvent,
    navigateToDetail: (Uuid) -> Unit,
    tagViewModel: TagFinishedListViewModel,
) {
    when (event) {
        is TagListEvent.ClickTag -> navigateToDetail(event.id)
        is TagListEvent.SwipeFinish -> tagViewModel.finish(id = event.id)
        is TagListEvent.SwipeRestart -> tagViewModel.restart(id = event.id)
        is TagListEvent.SwipeDelete -> tagViewModel.delete(id = event.id)
    }
}
