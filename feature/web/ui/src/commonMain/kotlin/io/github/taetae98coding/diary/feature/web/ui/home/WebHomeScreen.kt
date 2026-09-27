package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.web.WebListUndoSnackbarEffect
import kotlin.uuid.Uuid

@Composable
internal fun WebHomeScreen(
    navigateUp: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> WebHomeScaffoldComponentVisible,
    webViewModel: WebHomeViewModel,
    syncViewModel: WebHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val webPagingItems = webViewModel.webPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by webViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    WebListUndoSnackbarEffect(
        onRestore = webViewModel::restore,
        effect = webViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    WebHomeScaffold(
        onEvent = { event ->
            when (event) {
                is WebHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is WebHomeScaffoldEvent.ClickSearch -> navigateToSearch()
                is WebHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is WebHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is WebHomeScaffoldEvent.SelectSort -> webViewModel.select(sort = event.sort)
                is WebHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
                is WebHomeScaffoldEvent.ClickWeb -> navigateToDetail(event.id)
                is WebHomeScaffoldEvent.DeleteWeb -> webViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        webPagingItems = webPagingItems,
        uiStateProvider = { uiState },
        sortProvider = { sort },
        componentVisibleProvider = componentVisibleProvider,
    )
}
