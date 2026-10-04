package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
internal fun PlaylistHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> PlaylistHomeScaffoldComponentVisible,
    musicViewModel: PlaylistHomeViewModel,
    syncViewModel: SyncRefreshViewModel,
    downloadViewModel: PlaylistHomeDownloadViewModel,
    modifier: Modifier = Modifier,
) {
    val musicPagingItems = musicViewModel.musicPagingData.collectAsLazyPagingItems()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val downloadUiState by downloadViewModel.uiState.collectAsStateWithLifecycle()
    val uiState by musicViewModel.uiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    DownloadFailureSnackbarEffect(
        effect = downloadViewModel.effect,
        snackbarHostState = snackbarHostState,
    )
    DeleteUndoSnackbarEffect(
        onRestore = musicViewModel::restore,
        effect = musicViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    PlaylistHomeScaffold(
        onEvent = { event ->
            when (event) {
                is PlaylistHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is PlaylistHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is PlaylistHomeScaffoldEvent.ClickDownload -> downloadViewModel.download(sort = uiState.sort)
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
        syncUiStateProvider = { syncUiState },
        uiStateProvider = { uiState },
        downloadUiStateProvider = { downloadUiState },
        snackbarHostState = snackbarHostState,
        componentVisibleProvider = componentVisibleProvider,
    )
}
