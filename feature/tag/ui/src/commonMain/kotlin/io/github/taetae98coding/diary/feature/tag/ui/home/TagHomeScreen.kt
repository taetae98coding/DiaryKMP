package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.compose.tag.list.TagListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun TagHomeScreen(
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
    navigateToFinishedList: () -> Unit,
    navigateToSearch: () -> Unit,
    componentVisibleProvider: () -> TagHomeScaffoldComponentVisible,
    gridState: LazyGridState,
    tagViewModel: TagHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val filterUiState by tagViewModel.filterUiState.collectAsStateWithLifecycle()
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

    TagHomeScaffold(
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        gridState = gridState,
        tagPagingItems = tagPagingItems,
        onEvent = { event ->
            when (event) {
                is TagHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is TagHomeScaffoldEvent.ClickFilter -> navigateToFilter()
                is TagHomeScaffoldEvent.ClickFinishedList -> navigateToFinishedList()
                is TagHomeScaffoldEvent.ClickSearch -> navigateToSearch()
                is TagHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is TagHomeScaffoldEvent.SelectSort -> tagViewModel.select(sort = event.sort)
                is TagHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onTagListEvent = { event ->
            handleTagListEvent(event = event, navigateToDetail = navigateToDetail, tagViewModel = tagViewModel)
        },
        modifier =
            modifier.keyShortcut(isEnableProvider = { componentVisibleProvider().isAddButtonVisible }) { keyEvent ->
                if (keyEvent.isAddShortcut()) {
                    navigateToAdd()
                    true
                } else {
                    false
                }
            },
        uiStateProvider = { uiState },
        filterUiStateProvider = { filterUiState },
        sortProvider = { sortUiState.sort },
        componentVisibleProvider = componentVisibleProvider,
    )
}

private fun handleTagListEvent(
    event: TagListEvent,
    navigateToDetail: (Uuid) -> Unit,
    tagViewModel: TagHomeViewModel,
) {
    when (event) {
        is TagListEvent.ClickTag -> navigateToDetail(event.id)
        is TagListEvent.SwipeFinish -> tagViewModel.finish(id = event.id)
        is TagListEvent.SwipeRestart -> tagViewModel.restart(id = event.id)
        is TagListEvent.SwipeDelete -> tagViewModel.delete(id = event.id)
    }
}
