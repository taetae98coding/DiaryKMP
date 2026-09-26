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
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.collectMemoFormSelectablePagingItems
import io.github.taetae98coding.diary.feature.memo.ui.form.handleMemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiSettingRequiredEffect
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.handleMemoGeminiEvent
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
    navigateToContactAdd: () -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
    initialDateRange: MemoAddNavKey.InitialDateRange?,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> MemoAddScaffoldComponentVisible,
    isStandalone: Boolean,
    addViewModel: MemoAddViewModel,
    tagViewModel: MemoAddTagViewModel,
    webViewModel: MemoAddWebViewModel,
    contactViewModel: MemoAddContactViewModel,
    placeViewModel: MemoAddPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    geminiViewModel: MemoGeminiViewModel,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberMemoAddFormState(initialDateTime = initialDateRange?.toInitialDateTime())
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
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
    val selectablePagingItems = collectMemoFormSelectablePagingItems(tag = tagViewModel.selectableTagPagingData, web = webViewModel.selectableWebPagingData, contact = contactViewModel.selectableContactPagingData, place = placeViewModel.selectablePlacePagingData)

    MemoAddEnterEffect(tagAddRequestKey = tagAddRequestKey, tagViewModel = tagViewModel, webViewModel = webViewModel, contactViewModel = contactViewModel, placeViewModel = placeViewModel, placeMapViewModel = placeMapViewModel)
    MemoAddFormEffect(scaffoldState = scaffoldState, addViewModel = addViewModel, geminiViewModel = geminiViewModel)

    MemoAddScaffold(
        state = scaffoldState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        onEvent = { event ->
            handleMemoAddEvent(event = event, addViewModel = addViewModel, tagViewModel = tagViewModel, webViewModel = webViewModel, contactViewModel = contactViewModel, placeViewModel = placeViewModel, geminiViewModel = geminiViewModel, scaffoldState = scaffoldState, navigateUp = navigateUp)
        },
        onFormEvent = { event ->
            handleMemoFormEvent(
                event = event,
                state = scaffoldState,
                selectablePagingItems = selectablePagingItems,
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
        onTagPickerEvent = { event -> handleMemoAddTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        onWebPickerEvent = { event -> handleMemoAddWebPickerEvent(event = event, webViewModel = webViewModel, navigateToWebAdd = navigateToWebAdd) },
        onContactPickerEvent = { event -> handleMemoAddContactPickerEvent(event = event, contactViewModel = contactViewModel, navigateToContactAdd = navigateToContactAdd) },
        onPlacePickerEvent = { event -> handleMemoAddPlacePickerEvent(event = event, placeViewModel = placeViewModel, navigateToPlaceAdd = navigateToPlaceAdd) },
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

@Composable
private fun MemoAddEnterEffect(
    tagAddRequestKey: Uuid,
    tagViewModel: MemoAddTagViewModel,
    webViewModel: MemoAddWebViewModel,
    contactViewModel: MemoAddContactViewModel,
    placeViewModel: MemoAddPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        placeMapViewModel.fetchCurrentLocation()
    }

    MemoTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::selectTag)
    MemoWebAddedResultEffect(onWebAdded = webViewModel::selectWeb)
    MemoContactAddedResultEffect(onContactAdded = contactViewModel::selectContact)
    MemoPlaceAddedResultEffect(onPlaceAdded = placeViewModel::selectPlace)
}

@Composable
private fun MemoAddFormEffect(
    scaffoldState: MemoFormState,
    addViewModel: MemoAddViewModel,
    geminiViewModel: MemoGeminiViewModel,
) {
    DiaryTitleInputFocusEffect(state = scaffoldState.titleState)
    MemoAddScreenEffect(effect = addViewModel.effect, scaffoldState = scaffoldState)
    MemoGeminiSettingRequiredEffect(hostState = scaffoldState.hostState, effect = geminiViewModel.effect)
}

private fun MemoAddNavKey.InitialDateRange.toInitialDateTime(): DiaryDateTimeInputValue = DiaryDateTimeInputValue.AllDay(dateRange = start..endInclusive)
