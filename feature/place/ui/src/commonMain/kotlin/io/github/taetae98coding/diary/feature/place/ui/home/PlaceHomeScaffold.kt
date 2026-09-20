package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toCoordinate
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_home_add_button_content_description
import io.github.taetae98coding.diary.feature.place.ui.previewPlace
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceHomeScaffold(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: PlaceHomeScaffoldState = rememberPlaceHomeScaffoldState(),
    sortSheetState: DialogState = rememberDialogState(),
    uiStateProvider: () -> PlaceHomeUiState = { PlaceHomeUiState.Loading },
    placeListUiStateProvider: () -> PlaceHomePlaceListUiState = { PlaceHomePlaceListUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    syncUiStateProvider: () -> PlaceHomeSyncUiState = { PlaceHomeSyncUiState() },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    val uiState = uiStateProvider()
    val mapState = rememberPlaceHomeMapState(uiState = uiState)

    PlaceHomeMoveMapEffect(
        mapState = mapState,
        onEvent = onEvent,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            PlaceHomeTopBar(
                onEvent = onEvent,
                state = state,
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = {
                    val coordinate =
                        when (state.viewMode) {
                            PlaceHomeViewMode.MAP -> mapState.coordinate?.toCoordinate()
                            PlaceHomeViewMode.LIST -> null
                        }

                    onEvent(PlaceHomeScaffoldEvent.ClickAdd(coordinate = coordinate))
                },
                contentDescription = stringResource(Res.string.place_home_add_button_content_description),
            )
        },
    ) { paddingValues ->
        Body(
            onEvent = onEvent,
            mapState = mapState,
            uiState = uiState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            placeListUiStateProvider = placeListUiStateProvider,
            placePagingItems = placePagingItems,
            syncUiStateProvider = syncUiStateProvider,
            sortProvider = sortProvider,
        )
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(PlaceHomeScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@Composable
private fun Body(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    mapState: DiaryMapState,
    uiState: PlaceHomeUiState,
    modifier: Modifier = Modifier,
    state: PlaceHomeScaffoldState = rememberPlaceHomeScaffoldState(),
    placeListUiStateProvider: () -> PlaceHomePlaceListUiState = { PlaceHomePlaceListUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    syncUiStateProvider: () -> PlaceHomeSyncUiState = { PlaceHomeSyncUiState() },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    DiaryCrossfade(
        targetState = state.viewMode,
        modifier = modifier,
    ) { viewMode ->
        when (viewMode) {
            PlaceHomeViewMode.MAP ->
                when (uiState) {
                    is PlaceHomeUiState.Loading -> Unit

                    is PlaceHomeUiState.Loaded ->
                        PlaceHomeContent(
                            onEvent = onEvent,
                            mapState = mapState,
                            placeListUiStateProvider = placeListUiStateProvider,
                            isRefreshingProvider = { syncUiStateProvider().isRefreshing },
                            sortProvider = sortProvider,
                        )
                }

            PlaceHomeViewMode.LIST ->
                PlaceHomePagingList(
                    onEvent = onEvent,
                    placePagingItems = placePagingItems,
                    isRefreshingProvider = { syncUiStateProvider().isRefreshing },
                    sortProvider = sortProvider,
                )
        }
    }
}

@ScreenPreview
@Composable
private fun PlaceHomeScaffoldPreview(
    @PreviewParameter(PlaceHomeViewModePreviewParameter::class) viewMode: PlaceHomeViewMode,
) {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        PlaceHomeScaffold(
            onEvent = {},
            state = PlaceHomeScaffoldState(initialViewMode = viewMode),
            uiStateProvider = { PlaceHomeUiState.Loaded(defaultProvider = MapProvider.NAVER) },
            placeListUiStateProvider = { PlaceHomePlaceListUiState(isLoaded = true, placeList = placeList) },
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
        )
    }
}
