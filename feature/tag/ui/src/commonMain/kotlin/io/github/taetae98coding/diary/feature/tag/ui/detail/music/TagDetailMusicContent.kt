package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.music.MusicListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailMusicContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMusicDetail: (Uuid) -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.MUSIC, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val musicViewModel = koinViewModel<TagDetailMusicViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<SyncRefreshViewModel>()
        val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val musicPagingItems = musicViewModel.musicPagingData.collectAsLazyPagingItems()
        val sortUiState by musicViewModel.sortUiState.collectAsStateWithLifecycle()
        val scopeUiState by musicViewModel.scopeUiState.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        MusicListUndoSnackbarEffect(
            onRestore = musicViewModel::restore,
            effect = musicViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

        TagDetailScopeEffect(
            onSelect = { scope -> musicViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailMusicTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailMusicContentEvent.ClickMusic -> navigateToMusicDetail(event.id)
                    is TagDetailMusicContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailMusicContentEvent.ClickSort -> sortSheetState.show()
                    is TagDetailMusicContentEvent.SelectSort -> musicViewModel.select(sort = event.sort)
                    is TagDetailMusicContentEvent.DeleteMusic -> musicViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            sortSheetState = sortSheetState,
            musicPagingItems = musicPagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            sortProvider = { sortUiState.sort },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
