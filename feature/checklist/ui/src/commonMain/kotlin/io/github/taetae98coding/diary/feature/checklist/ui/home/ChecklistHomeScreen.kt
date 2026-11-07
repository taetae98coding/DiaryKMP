package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEvent
import io.github.taetae98coding.diary.compose.checklist.ChecklistListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun ChecklistHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> ChecklistHomeScaffoldComponentVisible,
    checklistViewModel: ChecklistHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val checklistPagingItems = checklistViewModel.checklistPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ChecklistListUndoSnackbarEffect(
        onRestore = checklistViewModel::restore,
        effect = checklistViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    ChecklistHomeScaffold(
        onEvent = { event ->
            when (event) {
                is ChecklistHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is ChecklistHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is ChecklistHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onChecklistListEvent = { event ->
            when (event) {
                is ChecklistListEvent.ClickChecklist -> navigateToDetail(event.id)
                is ChecklistListEvent.SwipeDelete -> checklistViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        checklistPagingItems = checklistPagingItems,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
