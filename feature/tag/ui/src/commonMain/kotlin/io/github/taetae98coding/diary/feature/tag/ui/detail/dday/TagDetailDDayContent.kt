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
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.DDayListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
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
        val ddayViewModel = assistedMetroViewModel<TagDetailDDayViewModel, TagDetailDDayViewModel.Factory> { create(tagId = id) }
        val syncViewModel = metroViewModel<SyncRefreshViewModel>()
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
                    is TagDetailDDayContentEvent.Refresh -> syncViewModel.refresh()
                }
            },
            onDDayListEvent = { event ->
                when (event) {
                    is DDayListEvent.ClickDDay -> navigateToDDayDetail(event.id)
                    is DDayListEvent.SwipeDelete -> ddayViewModel.delete(id = event.id)
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
