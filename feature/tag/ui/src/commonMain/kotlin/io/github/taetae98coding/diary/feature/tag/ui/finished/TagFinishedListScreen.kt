package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import kotlin.uuid.Uuid

@Composable
internal fun TagFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    tagViewModel: TagFinishedListViewModel,
    syncViewModel: TagFinishedListSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val sort by tagViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    TagFinishedListScaffold(
        sortSheetState = sortSheetState,
        tagPagingItems = tagPagingItems,
        onEvent = { event ->
            when (event) {
                is TagFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is TagFinishedListScaffoldEvent.ClickSort -> sortSheetState.show()
                is TagFinishedListScaffoldEvent.SelectSort -> tagViewModel.select(sort = event.sort)
                is TagFinishedListScaffoldEvent.ClickTag -> navigateToDetail(event.id)
                is TagFinishedListScaffoldEvent.Refresh -> syncViewModel.refresh()
            }
        },
        modifier = modifier,
        uiStateProvider = { uiState },
        sortProvider = { sort },
    )
}
