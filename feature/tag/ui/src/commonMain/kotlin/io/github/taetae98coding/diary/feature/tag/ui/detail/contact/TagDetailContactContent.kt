package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

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
import io.github.taetae98coding.diary.compose.contact.ContactListEvent
import io.github.taetae98coding.diary.compose.contact.ContactListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailContactContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToContactDetail: (Uuid) -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.CONTACT, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val contactViewModel = assistedMetroViewModel<TagDetailContactViewModel, TagDetailContactViewModel.Factory> { create(tagId = id) }
        val syncViewModel = metroViewModel<SyncRefreshViewModel>()
        val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val contactPagingItems = contactViewModel.contactPagingData.collectAsLazyPagingItems()
        val sortUiState by contactViewModel.sortUiState.collectAsStateWithLifecycle()
        val scopeUiState by contactViewModel.scopeUiState.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        ContactListUndoSnackbarEffect(
            onRestore = contactViewModel::restore,
            effect = contactViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

        TagDetailScopeEffect(
            onSelect = { scope -> contactViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailContactTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailContactContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailContactContentEvent.ClickSort -> sortSheetState.show()
                    is TagDetailContactContentEvent.SelectSort -> contactViewModel.select(sort = event.sort)
                }
            },
            onContactListEvent = { event ->
                when (event) {
                    is ContactListEvent.ClickContact -> navigateToContactDetail(event.id)
                    is ContactListEvent.SwipeDelete -> contactViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            sortSheetState = sortSheetState,
            contactPagingItems = contactPagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            sortProvider = { sortUiState.sort },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
