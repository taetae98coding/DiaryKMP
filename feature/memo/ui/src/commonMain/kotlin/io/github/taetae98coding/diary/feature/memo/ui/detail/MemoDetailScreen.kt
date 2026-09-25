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
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.handleMemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiCloseEffect
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiSettingRequiredEffect
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.handleMemoGeminiEvent
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
    navigateToContactAdd: () -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible,
    isStandalone: Boolean,
    detailViewModel: MemoDetailViewModel,
    tagViewModel: MemoTagViewModel,
    webViewModel: MemoWebViewModel,
    contactViewModel: MemoContactViewModel,
    placeViewModel: MemoPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    geminiViewModel: MemoGeminiViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val webUiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val contactUiState by contactViewModel.uiState.collectAsStateWithLifecycle()
    val placeUiState by placeViewModel.uiState.collectAsStateWithLifecycle()
    val placeMapUiState by placeMapViewModel.uiState.collectAsStateWithLifecycle()
    val geminiUiState by geminiViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val webPagingItems = webViewModel.webPagingData.collectAsLazyPagingItems()
    val contactPagingItems = contactViewModel.contactPagingData.collectAsLazyPagingItems()
    val placePagingItems = placeViewModel.placePagingData.collectAsLazyPagingItems()
    val content = uiState as? MemoDetailUiState.Content

    MemoDetailEnterEffect(targetId = content?.id, tagAddRequestKey = tagAddRequestKey, tagViewModel = tagViewModel, webViewModel = webViewModel, contactViewModel = contactViewModel, placeViewModel = placeViewModel, placeMapViewModel = placeMapViewModel, geminiViewModel = geminiViewModel)

    key(content?.id) {
        val scaffoldState = rememberMemoDetailFormState(initialDetail = content?.detail ?: MemoDetail.EMPTY)

        MemoDetailTargetEffect(id = content?.id, scaffoldState = scaffoldState, detailViewModel = detailViewModel, geminiViewModel = geminiViewModel, navigateUp = navigateUp, navigateToCopiedMemo = navigateToCopiedMemo)

        MemoDetailScaffold(
            state = scaffoldState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            onEvent = { event -> handleMemoDetailEvent(event = event, detailViewModel = detailViewModel, geminiViewModel = geminiViewModel, scaffoldState = scaffoldState, navigateUp = navigateUp) },
            onFormEvent = { event ->
                handleMemoFormEvent(
                    event = event,
                    state = scaffoldState,
                    tagPagingItems = tagPagingItems,
                    webPagingItems = webPagingItems,
                    contactPagingItems = contactPagingItems,
                    placePagingItems = placePagingItems,
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                    navigateToWebAdd = navigateToWebAdd,
                    navigateToWebDetail = navigateToWebDetail,
                    navigateToContactAdd = navigateToContactAdd,
                    navigateToContactDetail = navigateToContactDetail,
                    navigateToPlaceAdd = navigateToPlaceAdd,
                    navigateToPlaceDetail = navigateToPlaceDetail,
                )
            },
            onTagPickerEvent = { event -> handleMemoDetailTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            onWebPickerEvent = { event -> handleMemoDetailWebPickerEvent(event = event, webViewModel = webViewModel, navigateToWebAdd = navigateToWebAdd) },
            onContactPickerEvent = { event -> handleMemoDetailContactPickerEvent(event = event, contactViewModel = contactViewModel, navigateToContactAdd = navigateToContactAdd) },
            onPlacePickerEvent = { event -> handleMemoDetailPlacePickerEvent(event = event, placeViewModel = placeViewModel, navigateToPlaceAdd = navigateToPlaceAdd) },
            onGeminiEvent = { event -> handleMemoGeminiEvent(event = event, geminiViewModel = geminiViewModel, state = scaffoldState) },
            onGeminiDismissRequest = geminiViewModel::close,
            modifier = modifier,
            tagUiStateProvider = { tagUiState },
            webUiStateProvider = { webUiState },
            webPagingItems = webPagingItems,
            contactUiStateProvider = { contactUiState },
            contactPagingItems = contactPagingItems,
            placeCardUiStateProvider = { MemoPlaceCardUiState(mapUiState = placeMapUiState, placeUiState = placeUiState) },
            placePagingItems = placePagingItems,
            geminiUiStateProvider = { geminiUiState },
            componentVisibleProvider = componentVisibleProvider,
            isStandalone = isStandalone,
        )
    }
}

@Composable
private fun MemoDetailTargetEffect(
    id: Uuid?,
    scaffoldState: MemoFormState,
    detailViewModel: MemoDetailViewModel,
    geminiViewModel: MemoGeminiViewModel,
    navigateUp: () -> Unit,
    navigateToCopiedMemo: (Uuid) -> Unit,
) {
    MemoDetailScreenEffect(effect = detailViewModel.effect, scaffoldState = scaffoldState, navigateUp = navigateUp, navigateToCopiedMemo = navigateToCopiedMemo)
    MemoGeminiSettingRequiredEffect(hostState = scaffoldState.hostState, effect = geminiViewModel.effect)

    if (id != null) {
        MemoCopiedResultEffect(id = id, scaffoldState = scaffoldState)
    }
}

@Composable
private fun MemoDetailEnterEffect(
    targetId: Uuid?,
    tagAddRequestKey: Uuid,
    tagViewModel: MemoTagViewModel,
    webViewModel: MemoWebViewModel,
    contactViewModel: MemoContactViewModel,
    placeViewModel: MemoPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    geminiViewModel: MemoGeminiViewModel,
) {
    MemoGeminiCloseEffect(targetId = targetId, geminiViewModel = geminiViewModel)

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        placeMapViewModel.fetchCurrentLocation()
    }

    MemoTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::selectTag,
    )
    MemoWebAddedResultEffect(onWebAdded = webViewModel::selectWeb)
    MemoContactAddedResultEffect(onContactAdded = contactViewModel::selectContact)
    MemoPlaceAddedResultEffect(onPlaceAdded = placeViewModel::selectPlace)
}
