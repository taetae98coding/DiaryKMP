package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBar
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_add_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_title
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_navigate_up_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaylistHomeScaffold(
    onEvent: (PlaylistHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    musicPagingItems: LazyPagingItems<Music> = remember { flowOf(PagingData.empty<Music>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> PlaylistHomeUiState = { PlaylistHomeUiState() },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.playlist_home_title),
                onNavigateUp = { onEvent(PlaylistHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.playlist_navigate_up_button_content_description),
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(PlaylistHomeScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.playlist_home_add_button_content_description),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryListSortBar(
                onClick = { onEvent(PlaylistHomeScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
            )

            PlaylistHomeList(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                musicPagingItems = musicPagingItems,
                isRefreshingProvider = { uiStateProvider().isRefreshing },
                sortProvider = sortProvider,
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(PlaylistHomeScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun PlaylistHomeScaffoldPreview() {
    DiaryTheme {
        PlaylistHomeScaffold(onEvent = {})
    }
}
