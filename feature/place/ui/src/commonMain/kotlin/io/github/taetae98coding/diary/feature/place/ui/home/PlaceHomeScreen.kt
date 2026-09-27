package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.place.PlaceListUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.map.PlaceHomeMapViewModel
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
    val snackbarHostState = remember { SnackbarHostState() }

    FetchCurrentLocationEffect(mapViewModel = mapViewModel)

    PlaceListUndoSnackbarEffect(
        onRestore = placeListViewModel::restore,
        effect = placeListViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    PlaceHomeScaffold(
        uiStateProvider = { uiState },
        placeListUiStateProvider = { placeListUiState },
        placePagingItems = placePagingItems,
        syncUiStateProvider = { syncUiState },
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        sortProvider = { sort },
        onEvent = { event ->
            when (event) {
                is PlaceHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is PlaceHomeScaffoldEvent.ClickSearch -> navigateToSearch()
                is PlaceHomeScaffoldEvent.ClickAdd -> navigateToAdd(event.coordinate)
                is PlaceHomeScaffoldEvent.ClickPlace -> navigateToDetail(event.id)
                is PlaceHomeScaffoldEvent.DeletePlace -> placeListViewModel.delete(id = event.id)
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
