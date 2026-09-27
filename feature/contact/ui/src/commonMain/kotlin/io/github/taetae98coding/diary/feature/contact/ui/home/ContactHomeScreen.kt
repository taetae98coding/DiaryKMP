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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ContactHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> ContactHomeScaffoldComponentVisible,
    contactViewModel: ContactHomeViewModel,
    syncViewModel: ContactHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val contactPagingItems = contactViewModel.contactPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by contactViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    ContactHomeUndoSnackbarEffect(
        onRestore = contactViewModel::restore,
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
        sortProvider = { sort },
        componentVisibleProvider = componentVisibleProvider,
    )
}

@Composable
private fun ContactHomeUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<ContactHomeEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val deletedMessage = stringResource(Res.string.contact_home_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.contact_home_undo_action),
        message = { value ->
            when (value) {
                is ContactHomeEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is ContactHomeEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
