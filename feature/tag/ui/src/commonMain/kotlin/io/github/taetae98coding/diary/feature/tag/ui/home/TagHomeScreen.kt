package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
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
    syncViewModel: TagHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val filterUiState by tagViewModel.filterUiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val sort by tagViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    TagHomeScaffold(
        sortSheetState = sortSheetState,
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
                is TagHomeScaffoldEvent.ClickTag -> navigateToDetail(event.id)
                is TagHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        modifier =
            modifier.keyShortcut { keyEvent ->
                if (keyEvent.isAddShortcut()) {
                    navigateToAdd()
                    true
                } else {
                    false
                }
            },
        uiStateProvider = { uiState },
        filterUiStateProvider = { filterUiState },
        sortProvider = { sort },
        componentVisibleProvider = componentVisibleProvider,
    )
}
