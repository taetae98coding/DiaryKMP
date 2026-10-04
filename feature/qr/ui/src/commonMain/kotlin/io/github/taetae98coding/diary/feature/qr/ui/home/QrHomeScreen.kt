package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel

@Composable
internal fun QrHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    qrViewModel: QrHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val qrPagingItems = qrViewModel.qrPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    DeleteUndoSnackbarEffect(
        onRestore = qrViewModel::restore,
        effect = qrViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    QrHomeScaffold(
        onEvent = { event ->
            when (event) {
                is QrHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is QrHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is QrHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
                is QrHomeScaffoldEvent.DeleteQr -> qrViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        qrPagingItems = qrPagingItems,
        uiStateProvider = { uiState },
    )
}
