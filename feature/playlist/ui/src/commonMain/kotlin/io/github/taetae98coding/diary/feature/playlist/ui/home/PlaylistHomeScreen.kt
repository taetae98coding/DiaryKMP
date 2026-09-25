package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_deleted_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PlaylistHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> PlaylistHomeScaffoldComponentVisible,
    musicViewModel: PlaylistHomeViewModel,
    syncViewModel: PlaylistHomeSyncViewModel,
    downloadViewModel: PlaylistHomeDownloadViewModel,
    modifier: Modifier = Modifier,
) {
    val musicPagingItems = musicViewModel.musicPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val downloadUiState by downloadViewModel.uiState.collectAsStateWithLifecycle()
    val sort by musicViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    PlaylistHomeScreenEffect(
        effect = downloadViewModel.effect,
        hostState = snackbarHostState,
    )
    PlaylistHomeUndoSnackbarEffect(
        onRestore = musicViewModel::restore,
        effect = musicViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    PlaylistHomeScaffold(
        onEvent = { event ->
            when (event) {
                is PlaylistHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is PlaylistHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is PlaylistHomeScaffoldEvent.ClickDownload -> downloadViewModel.download(sort = sort)
                is PlaylistHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is PlaylistHomeScaffoldEvent.SelectSort -> musicViewModel.select(sort = event.sort)
                is PlaylistHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
                is PlaylistHomeScaffoldEvent.ClickMusic -> navigateToDetail(event.id)
                is PlaylistHomeScaffoldEvent.DeleteMusic -> musicViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        sortSheetState = sortSheetState,
        musicPagingItems = musicPagingItems,
        uiStateProvider = { uiState },
        sortProvider = { sort },
        downloadUiStateProvider = { downloadUiState },
        snackbarHostState = snackbarHostState,
        componentVisibleProvider = componentVisibleProvider,
    )
}

@Composable
private fun PlaylistHomeUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<PlaylistHomeEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val deletedMessage = stringResource(Res.string.playlist_home_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.playlist_home_undo_action),
        message = { value ->
            when (value) {
                is PlaylistHomeEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is PlaylistHomeEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
