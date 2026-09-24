package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
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
