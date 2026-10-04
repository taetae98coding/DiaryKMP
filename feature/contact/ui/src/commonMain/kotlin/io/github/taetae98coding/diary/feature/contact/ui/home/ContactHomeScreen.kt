package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_deleted_message
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_undo_action
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ContactHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> ContactHomeScaffoldComponentVisible,
    contactViewModel: ContactHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val contactPagingItems = contactViewModel.contactPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sortUiState by contactViewModel.sortUiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    UndoSnackbarEffect(
        actionLabel = stringResource(Res.string.contact_home_undo_action),
        message = stringResource(Res.string.contact_home_deleted_message),
        onUndo = { value ->
            when (value) {
                is ContactHomeEffect.Deleted -> contactViewModel.restore(id = value.id)
            }
        },
        effect = contactViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    ContactHomeScaffold(
        onEvent = { event ->
            when (event) {
                is ContactHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is ContactHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is ContactHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is ContactHomeScaffoldEvent.SelectSort -> contactViewModel.select(sort = event.sort)
                is ContactHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
                is ContactHomeScaffoldEvent.ClickContact -> navigateToDetail(event.id)
                is ContactHomeScaffoldEvent.DeleteContact -> contactViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        contactPagingItems = contactPagingItems,
        uiStateProvider = { uiState },
        sortProvider = { sortUiState.sort },
        componentVisibleProvider = componentVisibleProvider,
    )
}
