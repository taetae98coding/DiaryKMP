package io.github.taetae98coding.diary.feature.tag.ui.detail.place

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
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.PlaceListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailSyncViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailPlaceContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToPlaceDetail: (Uuid) -> Unit,
    state: TagDetailPlaceState,
    mapViewModel: TagDetailPlaceMapViewModel,
    mapState: DiaryMapState,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.PLACE, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val placeViewModel = koinViewModel<TagDetailPlaceViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<TagDetailSyncViewModel>()
        val isRefreshing by syncViewModel.isRefreshing.collectAsStateWithLifecycle()
        val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
        val placeListUiState by placeViewModel.placeListUiState.collectAsStateWithLifecycle()
        val placePagingItems = placeViewModel.placePagingData.collectAsLazyPagingItems()
        val sort by placeViewModel.sort.collectAsStateWithLifecycle()
        val queryScope by placeViewModel.scope.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        PlaceListUndoSnackbarEffect(
            onRestore = placeViewModel::restore,
            effect = placeViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

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

                    is TagDetailPlaceContentEvent.DeletePlace -> placeViewModel.delete(id = event.id)

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
            mapState = mapState,
            sortSheetState = sortSheetState,
            uiStateProvider = { uiState },
            placeListUiStateProvider = { placeListUiState },
            placePagingItems = placePagingItems,
            isRefreshingProvider = { isRefreshing },
            sortProvider = { sort },
            scopeProvider = { queryScope },
        )
    }
}
