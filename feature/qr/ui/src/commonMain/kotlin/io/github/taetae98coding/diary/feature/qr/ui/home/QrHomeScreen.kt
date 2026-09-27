package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_deleted_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun QrHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    qrViewModel: QrHomeViewModel,
    syncViewModel: QrHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val qrPagingItems = qrViewModel.qrPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    QrHomeUndoSnackbarEffect(
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

@Composable
private fun QrHomeUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<QrHomeEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val deletedMessage = stringResource(Res.string.qr_home_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.qr_home_undo_action),
        message = { value ->
            when (value) {
                is QrHomeEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is QrHomeEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
