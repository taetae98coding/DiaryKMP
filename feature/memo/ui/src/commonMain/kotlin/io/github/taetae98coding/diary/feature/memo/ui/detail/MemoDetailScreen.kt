package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.form.handleMemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCardUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailScreen(
    navigateUp: () -> Unit,
    navigateToCopiedMemo: (Uuid) -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToWebAdd: () -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible,
    isStandalone: Boolean,
    detailViewModel: MemoDetailViewModel,
    tagViewModel: MemoTagViewModel,
    webViewModel: MemoWebViewModel,
    placeViewModel: MemoPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val webUiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val placeUiState by placeViewModel.uiState.collectAsStateWithLifecycle()
    val placeMapUiState by placeMapViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val webPagingItems = webViewModel.webPagingData.collectAsLazyPagingItems()
    val placePagingItems = placeViewModel.placePagingData.collectAsLazyPagingItems()
    val content = uiState as? MemoDetailUiState.Content

    MemoDetailEnterEffect(tagAddRequestKey = tagAddRequestKey, tagViewModel = tagViewModel, webViewModel = webViewModel, placeViewModel = placeViewModel, placeMapViewModel = placeMapViewModel)

    key(content?.id) {
        val scaffoldState = rememberMemoDetailFormState(initialDetail = content?.detail ?: MemoDetail.EMPTY)

        MemoDetailScreenEffect(effect = detailViewModel.effect, scaffoldState = scaffoldState, navigateUp = navigateUp, navigateToCopiedMemo = navigateToCopiedMemo)

        if (content != null) {
            MemoCopiedResultEffect(id = content.id, scaffoldState = scaffoldState)
        }

        MemoDetailScaffold(
            state = scaffoldState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            onEvent = { event -> handleMemoDetailEvent(event = event, detailViewModel = detailViewModel, scaffoldState = scaffoldState, navigateUp = navigateUp) },
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
            onTagPickerEvent = { event -> handleMemoDetailTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            onWebPickerEvent = { event -> handleMemoDetailWebPickerEvent(event = event, webViewModel = webViewModel, navigateToWebAdd = navigateToWebAdd) },
            onPlacePickerEvent = { event -> handleMemoDetailPlacePickerEvent(event = event, placeViewModel = placeViewModel, navigateToPlaceAdd = navigateToPlaceAdd) },
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
}

@Composable
private fun MemoDetailEnterEffect(
    tagAddRequestKey: Uuid,
    tagViewModel: MemoTagViewModel,
    webViewModel: MemoWebViewModel,
    placeViewModel: MemoPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        placeMapViewModel.fetchCurrentLocation()
    }

    MemoTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::selectTag,
    )
    MemoWebAddedResultEffect(onWebAdded = webViewModel::selectWeb)
    MemoPlaceAddedResultEffect(onPlaceAdded = placeViewModel::selectPlace)
}
