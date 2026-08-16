package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
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

    ContactHomeScaffold(
        onEvent = { event ->
            when (event) {
                is ContactHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is ContactHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is ContactHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is ContactHomeScaffoldEvent.SelectSort -> contactViewModel.select(sort = event.sort)
                is ContactHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
                is ContactHomeScaffoldEvent.ClickContact -> navigateToDetail(event.id)
            }
        },
        modifier = modifier,
        sortSheetState = sortSheetState,
        contactPagingItems = contactPagingItems,
        uiStateProvider = { uiState },
        sortProvider = { sort },
        componentVisibleProvider = componentVisibleProvider,
    )
}
