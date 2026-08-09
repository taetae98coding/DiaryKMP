package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.uuid.Uuid

@Composable
internal fun PlaceHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: (Coordinate?) -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToSearch: () -> Unit,
    mapViewModel: PlaceHomeMapViewModel,
    placeListViewModel: PlaceHomePlaceListViewModel,
    syncViewModel: PlaceHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val placeListUiState by placeListViewModel.placeListUiState.collectAsStateWithLifecycle()
    val placePagingItems = placeListViewModel.placePagingData.collectAsLazyPagingItems()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by placeListViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    FetchCurrentLocationEffect(mapViewModel = mapViewModel)

    PlaceHomeScaffold(
        uiStateProvider = { uiState },
        placeListUiStateProvider = { placeListUiState },
        placePagingItems = placePagingItems,
        syncUiStateProvider = { syncUiState },
        sortSheetState = sortSheetState,
        sortProvider = { sort },
        onEvent = { event ->
            when (event) {
                is PlaceHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is PlaceHomeScaffoldEvent.ClickSearch -> navigateToSearch()
                is PlaceHomeScaffoldEvent.ClickAdd -> navigateToAdd(event.coordinate)
                is PlaceHomeScaffoldEvent.ClickPlace -> navigateToDetail(event.id)
                is PlaceHomeScaffoldEvent.MoveMap -> placeListViewModel.updateVisibleBounds(event.bounds)
                is PlaceHomeScaffoldEvent.Refresh -> syncViewModel.refresh()
                is PlaceHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is PlaceHomeScaffoldEvent.SelectSort -> placeListViewModel.select(sort = event.sort)
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun FetchCurrentLocationEffect(mapViewModel: PlaceHomeMapViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        mapViewModel.fetchCurrentLocation()
    }
}
