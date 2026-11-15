package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.rememberMemoDetailSharer
import io.github.taetae98coding.diary.feature.memo.ui.detail.share.sharePeriodText
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.collectMemoFormSelectablePagingItems
import io.github.taetae98coding.diary.feature.memo.ui.form.handleMemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiCloseEffect
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiSettingRequiredEffect
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.handleMemoGeminiEvent
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceAddedResultEffect
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCardUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapViewModel
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
    navigateToGeminiSetting: () -> Unit,
    addRequestKey: Uuid,
    componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible,
    isStandalone: Boolean,
    detailViewModel: MemoDetailViewModel,
    tagViewModel: MemoTagViewModel,
    webViewModel: MemoWebViewModel,
    contactViewModel: MemoContactViewModel,
    placeViewModel: MemoPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    placePickerMapViewModel: MemoPlacePickerMapViewModel,
    geminiViewModel: MemoGeminiViewModel,
    contentSharerFactory: ContentSharerFactory,
    modifier: Modifier = Modifier,
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val webUiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val contactUiState by contactViewModel.uiState.collectAsStateWithLifecycle()
    val placeCardUiStateProvider = rememberMemoPlaceCardUiStateProvider(placeViewModel = placeViewModel, placeMapViewModel = placeMapViewModel, placePickerMapViewModel = placePickerMapViewModel)
    val geminiUiState by geminiViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val webPagingItems = webViewModel.webPagingData.collectAsLazyPagingItems()
    val contactPagingItems = contactViewModel.contactPagingData.collectAsLazyPagingItems()
    val placePagingItems = placeViewModel.placePagingData.collectAsLazyPagingItems()
    val selectablePagingItems = collectMemoFormSelectablePagingItems(tag = tagViewModel.selectableTagPagingData, web = webViewModel.selectableWebPagingData, contact = contactViewModel.selectableContactPagingData, place = placeViewModel.selectablePlacePagingData)
    val shownContent = rememberShownMemoDetailContent(uiState = uiState)

    MemoDetailEnterEffect(targetId = shownContent?.id, addRequestKey = addRequestKey, tagViewModel = tagViewModel, webViewModel = webViewModel, contactViewModel = contactViewModel, placeViewModel = placeViewModel, placeMapViewModel = placeMapViewModel, geminiViewModel = geminiViewModel)

    key(shownContent?.id) {
        val scaffoldState = rememberMemoDetailFormState(initialDetail = shownContent?.detail ?: MemoDetail.EMPTY)
        val shareHandler = rememberMemoDetailShareHandler(contentSharerFactory = contentSharerFactory, scaffoldState = scaffoldState, uiStateProvider = { uiState })

        MemoDetailTargetEffect(id = shownContent?.id, scaffoldState = scaffoldState, detailViewModel = detailViewModel, geminiViewModel = geminiViewModel, navigateUp = navigateUp, navigateToCopiedMemo = navigateToCopiedMemo, navigateToGeminiSetting = navigateToGeminiSetting)

        MemoDetailScaffold(
            state = scaffoldState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = { uiState },
            onEvent = { event -> handleMemoDetailEvent(event = event, detailViewModel = detailViewModel, geminiViewModel = geminiViewModel, scaffoldState = scaffoldState, navigateUp = navigateUp, onShareEvent = shareHandler.onEvent) },
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
            onTagPickerEvent = { event -> handleMemoDetailTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
            onWebPickerEvent = { event -> handleMemoDetailWebPickerEvent(event = event, webViewModel = webViewModel, navigateToWebAdd = navigateToWebAdd) },
            onContactPickerEvent = { event -> handleMemoDetailContactPickerEvent(event = event, contactViewModel = contactViewModel, navigateToContactAdd = navigateToContactAdd) },
            onPlacePickerEvent = { event -> handleMemoDetailPlacePickerEvent(event = event, placeViewModel = placeViewModel, placePickerMapViewModel = placePickerMapViewModel, navigateToPlaceAdd = navigateToPlaceAdd) },
            onGeminiEvent = { event -> handleMemoGeminiEvent(event = event, geminiViewModel = geminiViewModel, state = scaffoldState) },
            onGeminiDismissRequest = geminiViewModel::close,
            modifier = modifier,
            tagUiStateProvider = { tagUiState },
            webUiStateProvider = { webUiState },
            webPagingItems = webPagingItems,
            contactUiStateProvider = { contactUiState },
            contactPagingItems = contactPagingItems,
            placeCardUiStateProvider = placeCardUiStateProvider,
            placePagingItems = placePagingItems,
            geminiUiStateProvider = { geminiUiState },
            componentVisibleProvider = componentVisibleProvider,
            isStandalone = isStandalone,
            isShareSupported = shareHandler.isSupported,
            shareSheetState = shareHandler.sheetState,
        )
    }
}

@Stable
private class MemoDetailShareHandler(
    val isSupported: Boolean,
    val sheetState: DialogState,
    val onEvent: (MemoDetailScaffoldEvent.Share) -> Unit,
)

@Composable
private fun rememberMemoDetailShareHandler(
    contentSharerFactory: ContentSharerFactory,
    scaffoldState: MemoFormState,
    uiStateProvider: () -> MemoDetailUiState,
): MemoDetailShareHandler {
    val sheetState = rememberDialogState()
    val sharer = rememberMemoDetailSharer(contentSharerFactory = contentSharerFactory, snackbarHostState = scaffoldState.snackbarHostState)
    val periodText by rememberUpdatedState(scaffoldState.sharePeriodText())
    val latestUiStateProvider by rememberUpdatedState(uiStateProvider)
    val coroutineScope = rememberCoroutineScope()

    return remember(sheetState, sharer, scaffoldState, coroutineScope) {
        MemoDetailShareHandler(
            isSupported = sharer.support.isTextSupported,
            sheetState = sheetState,
            onEvent = { event ->
                handleMemoDetailShareEvent(
                    event = event,
                    content = latestUiStateProvider() as? MemoDetailUiState.Content,
                    scaffoldState = scaffoldState,
                    periodText = periodText,
                    sharer = sharer,
                    shareSheetState = sheetState,
                    coroutineScope = coroutineScope,
                )
            },
        )
    }
}

@Composable
private fun rememberMemoPlaceCardUiStateProvider(
    placeViewModel: MemoPlaceViewModel,
    placeMapViewModel: MemoPlaceMapViewModel,
    placePickerMapViewModel: MemoPlacePickerMapViewModel,
): () -> MemoPlaceCardUiState {
    val placeUiState = placeViewModel.uiState.collectAsStateWithLifecycle()
    val mapUiState = placeMapViewModel.uiState.collectAsStateWithLifecycle()
    val pickerMapUiState = placePickerMapViewModel.uiState.collectAsStateWithLifecycle()

    return remember(placeUiState, mapUiState, pickerMapUiState) {
        { MemoPlaceCardUiState(mapUiState = mapUiState.value, placeUiState = placeUiState.value, pickerMapUiState = pickerMapUiState.value) }
    }
}

// 내용을 한 번 보인 뒤 조회가 잠시 끊겨 로딩으로 돌아가도 입력 상태를 새로 만들지 않도록, 마지막으로 보인 내용을 대상으로 붙잡아 둔다.
@Composable
private fun rememberShownMemoDetailContent(uiState: MemoDetailUiState): MemoDetailUiState.Content? {
    val content = uiState as? MemoDetailUiState.Content
    val lastContent = remember { mutableStateOf<MemoDetailUiState.Content?>(null) }

    SideEffect {
        if (content != null) lastContent.value = content
    }

    return content ?: lastContent.value
}

@Composable
private fun MemoDetailTargetEffect(
    id: Uuid?,
    scaffoldState: MemoFormState,
    detailViewModel: MemoDetailViewModel,
    geminiViewModel: MemoGeminiViewModel,
    navigateUp: () -> Unit,
    navigateToCopiedMemo: (Uuid) -> Unit,
    navigateToGeminiSetting: () -> Unit,
) {
    MemoDetailScreenEffect(effect = detailViewModel.effect, scaffoldState = scaffoldState, navigateUp = navigateUp, navigateToCopiedMemo = navigateToCopiedMemo)
    MemoGeminiSettingRequiredEffect(navigateToGeminiSetting = navigateToGeminiSetting, effect = geminiViewModel.effect)

    if (id != null) {
        MemoCopiedResultEffect(id = id, scaffoldState = scaffoldState)
    }
}

@Composable
private fun MemoDetailEnterEffect(
    targetId: Uuid?,
    addRequestKey: Uuid,
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
        requestKey = addRequestKey,
        onTagAdded = tagViewModel::selectTag,
    )
    MemoWebAddedResultEffect(requestKey = addRequestKey, onWebAdded = webViewModel::selectWeb)
    MemoContactAddedResultEffect(requestKey = addRequestKey, onContactAdded = contactViewModel::selectContact)
    MemoPlaceAddedResultEffect(requestKey = addRequestKey, onPlaceAdded = placeViewModel::selectPlace)
}
