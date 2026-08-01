package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.ui.form.handleMemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCardUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebAddedResultEffect
import kotlin.uuid.Uuid

@Composable
internal fun MemoAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToWebAdd: () -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    initialDateRange: MemoAddNavKey.InitialDateRange?,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> MemoAddScaffoldComponentVisible,
    isStandalone: Boolean,
    addViewModel: MemoAddViewModel,
    tagViewModel: MemoAddTagViewModel,
    webViewModel: MemoAddWebViewModel,
    placeViewModel: MemoAddPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberMemoAddFormState(initialDateTime = initialDateRange?.toInitialDateTime())
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val webUiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val placeUiState by placeViewModel.uiState.collectAsStateWithLifecycle()
    val placeMapUiState by placeMapViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val webPagingItems = webViewModel.webPagingData.collectAsLazyPagingItems()
    val placePagingItems = placeViewModel.placePagingData.collectAsLazyPagingItems()

    DiaryTitleInputFocusEffect(state = scaffoldState.titleState)
    FetchCurrentLocationEffect(placeMapViewModel = placeMapViewModel)
    MemoAddScreenEffect(
        effect = addViewModel.effect,
        scaffoldState = scaffoldState,
    )
    MemoTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::selectTag)
    MemoWebAddedResultEffect(onWebAdded = webViewModel::selectWeb)
    MemoPlaceAddedResultEffect(onPlaceAdded = placeViewModel::selectPlace)

    MemoAddScaffold(
        state = scaffoldState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        onEvent = { event -> handleMemoAddEvent(event = event, addViewModel = addViewModel, tagViewModel = tagViewModel, webViewModel = webViewModel, placeViewModel = placeViewModel, scaffoldState = scaffoldState, navigateUp = navigateUp) },
        onFormEvent = { event ->
            handleMemoFormEvent(
                event = event,
                state = scaffoldState,
                tagPagingItems = tagPagingItems,
                webPagingItems = webPagingItems,
                placePagingItems = placePagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
                navigateToWebAdd = navigateToWebAdd,
                navigateToWebDetail = navigateToWebDetail,
                navigateToPlaceAdd = navigateToPlaceAdd,
                navigateToPlaceDetail = navigateToPlaceDetail,
            )
        },
        onTagPickerEvent = { event -> handleMemoAddTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        onWebPickerEvent = { event -> handleMemoAddWebPickerEvent(event = event, webViewModel = webViewModel, navigateToWebAdd = navigateToWebAdd) },
        onPlacePickerEvent = { event -> handleMemoAddPlacePickerEvent(event = event, placeViewModel = placeViewModel, navigateToPlaceAdd = navigateToPlaceAdd) },
        modifier = modifier,
        tagUiStateProvider = { tagUiState },
        webUiStateProvider = { webUiState },
        webPagingItems = webPagingItems,
        placeCardUiStateProvider = { MemoPlaceCardUiState(mapUiState = placeMapUiState, placeUiState = placeUiState) },
        placePagingItems = placePagingItems,
        componentVisibleProvider = componentVisibleProvider,
        isStandalone = isStandalone,
    )
}

@Composable
private fun FetchCurrentLocationEffect(placeMapViewModel: MemoPlaceMapViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        placeMapViewModel.fetchCurrentLocation()
    }
}

private fun MemoAddNavKey.InitialDateRange.toInitialDateTime(): DiaryDateTimeInputValue = DiaryDateTimeInputValue.AllDay(dateRange = start..endInclusive)
