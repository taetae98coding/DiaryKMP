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
import io.github.taetae98coding.diary.compose.checklist.ChecklistListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
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
        val checklistViewModel = koinViewModel<TagDetailChecklistViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<SyncRefreshViewModel>()
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
                    is TagDetailChecklistContentEvent.ClickChecklist -> navigateToChecklistDetail(event.id)
                    is TagDetailChecklistContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailChecklistContentEvent.DeleteChecklist -> checklistViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            checklistPagingItems = checklistPagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
