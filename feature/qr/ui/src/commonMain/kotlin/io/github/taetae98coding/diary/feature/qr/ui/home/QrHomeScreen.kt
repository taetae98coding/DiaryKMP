package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.qr.QrListEvent
import io.github.taetae98coding.diary.compose.qr.QrListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun QrHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> QrHomeScaffoldComponentVisible,
    qrViewModel: QrHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val qrPagingItems = qrViewModel.qrPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    QrListUndoSnackbarEffect(
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
            }
        },
        onQrListEvent = { event ->
            when (event) {
                is QrListEvent.ClickQr -> navigateToDetail(event.id)
                is QrListEvent.SwipeDelete -> qrViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        qrPagingItems = qrPagingItems,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
