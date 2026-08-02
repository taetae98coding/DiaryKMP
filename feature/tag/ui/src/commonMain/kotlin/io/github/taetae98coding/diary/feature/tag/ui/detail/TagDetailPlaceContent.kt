package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailPlaceContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToPlaceDetail: (Uuid) -> Unit,
    state: TagDetailPlaceState,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.PLACE, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val placeViewModel = koinViewModel<TagDetailPlaceViewModel> { parametersOf(id) }
        val mapViewModel = koinViewModel<TagDetailPlaceMapViewModel>()
        val syncViewModel = koinViewModel<TagDetailSyncViewModel>()
        val isRefreshing by syncViewModel.isRefreshing.collectAsStateWithLifecycle()
        val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
        val placeListUiState by placeViewModel.placeListUiState.collectAsStateWithLifecycle()
        val placePagingItems = placeViewModel.placePagingData.collectAsLazyPagingItems()
        val sort by placeViewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        TagDetailScopeEffect(
            onSelect = { scope -> placeViewModel.select(scope = scope) },
            state = scopeState,
        )

        TagDetailPlaceFetchCurrentLocationEffect(
            mapViewModel = mapViewModel,
            state = state,
        )

        TagDetailPlaceTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailPlaceContentEvent.ClickPlace -> navigateToPlaceDetail(event.id)

                    is TagDetailPlaceContentEvent.MoveMap -> {
                        placeViewModel.updateVisibleBounds(event.bounds)
                        state.moveMap(coordinate = event.coordinate)
                    }

                    is TagDetailPlaceContentEvent.Refresh -> syncViewModel.refresh()

                    is TagDetailPlaceContentEvent.ClickSort -> sortSheetState.show()

                    is TagDetailPlaceContentEvent.SelectSort -> placeViewModel.select(sort = event.sort)
                }
            },
            modifier = modifier,
            state = state,
            sortSheetState = sortSheetState,
            uiStateProvider = { uiState },
            placeListUiStateProvider = { placeListUiState },
            placePagingItems = placePagingItems,
            isRefreshingProvider = { isRefreshing },
            sortProvider = { sort },
        )
    }
}
