package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

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
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEvent
import io.github.taetae98coding.diary.compose.checklist.ChecklistListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailChecklistContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToChecklistDetail: (Uuid) -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.CHECKLIST, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val checklistViewModel = assistedMetroViewModel<TagDetailChecklistViewModel, TagDetailChecklistViewModel.Factory> { create(tagId = id) }
        val syncViewModel = metroViewModel<SyncRefreshViewModel>()
        val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val checklistPagingItems = checklistViewModel.checklistPagingData.collectAsLazyPagingItems()
        val scopeUiState by checklistViewModel.scopeUiState.collectAsStateWithLifecycle()

        ChecklistListUndoSnackbarEffect(
            onRestore = checklistViewModel::restore,
            effect = checklistViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

        TagDetailScopeEffect(
            onSelect = { scope -> checklistViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailChecklistTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailChecklistContentEvent.Refresh -> syncViewModel.refresh()
                }
            },
            onChecklistListEvent = { event ->
                when (event) {
                    is ChecklistListEvent.ClickChecklist -> navigateToChecklistDetail(event.id)
                    is ChecklistListEvent.SwipeDelete -> checklistViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            checklistPagingItems = checklistPagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
