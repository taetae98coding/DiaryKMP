package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

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
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.DDayListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailDDayContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToDDayDetail: (Uuid) -> Unit,
    scopeState: TagDetailScopeState,
    listState: DDayListState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.DDAY, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val ddayViewModel = koinViewModel<TagDetailDDayViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<SyncRefreshViewModel>()
        val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val ddayPagingItems = ddayViewModel.ddayPagingData.collectAsLazyPagingItems()
        val scopeUiState by ddayViewModel.scopeUiState.collectAsStateWithLifecycle()

        DDayListUndoSnackbarEffect(
            onRestore = ddayViewModel::restore,
            effect = ddayViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

        TagDetailScopeEffect(
            onSelect = { scope -> ddayViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailDDayTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailDDayContentEvent.ClickDDay -> navigateToDDayDetail(event.id)
                    is TagDetailDDayContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailDDayContentEvent.DeleteDDay -> ddayViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            listState = listState,
            ddayPagingItems = ddayPagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
