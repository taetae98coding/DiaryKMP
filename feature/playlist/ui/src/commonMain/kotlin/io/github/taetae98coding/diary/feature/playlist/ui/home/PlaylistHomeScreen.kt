package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier,
) {
    val musicPagingItems = musicViewModel.musicPagingData.collectAsLazyPagingItems()
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by musicViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    PlaylistHomeScaffold(
        onEvent = { event ->
            when (event) {
                is PlaylistHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is PlaylistHomeScaffoldEvent.ClickAdd -> navigateToAdd()
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
        componentVisibleProvider = componentVisibleProvider,
    )
}
