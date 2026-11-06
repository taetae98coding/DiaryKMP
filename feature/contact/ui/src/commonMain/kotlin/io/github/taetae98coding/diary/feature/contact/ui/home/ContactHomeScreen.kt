package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.contact.ContactListEvent
import io.github.taetae98coding.diary.compose.contact.ContactListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
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
    val filterUiState by contactViewModel.filterUiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val filterSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    ContactListUndoSnackbarEffect(
        onRestore = { id -> contactViewModel.restore(id = id) },
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
                is ContactHomeScaffoldEvent.ClickFilter -> filterSheetState.show()
                is ContactHomeScaffoldEvent.SetBirthdayExistence -> contactViewModel.setBirthdayExistence(existence = event.existence)
                is ContactHomeScaffoldEvent.SetBirthYearExistence -> contactViewModel.setBirthYearExistence(existence = event.existence)
                is ContactHomeScaffoldEvent.SetPhoneNumberExistence -> contactViewModel.setPhoneNumberExistence(existence = event.existence)
                is ContactHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        onContactListEvent = { event ->
            when (event) {
                is ContactListEvent.ClickContact -> navigateToDetail(event.id)
                is ContactListEvent.SwipeDelete -> contactViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        sortSheetState = sortSheetState,
        filterSheetState = filterSheetState,
        snackbarHostState = snackbarHostState,
        contactPagingItems = contactPagingItems,
        uiStateProvider = { uiState },
        sortProvider = { sortUiState.sort },
        filterUiStateProvider = { filterUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
