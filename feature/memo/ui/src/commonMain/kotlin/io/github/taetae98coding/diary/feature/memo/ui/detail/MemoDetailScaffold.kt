package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.toCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiDialogEvent
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiDialogHost
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceCardUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerDialogHost
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.place.rememberMemoPlaceMapState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagPickerDialogHost
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebPickerDialogHost
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebPickerEvent
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailScaffold(
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    onFormEvent: (MemoFormEvent) -> Unit,
    onTagPickerEvent: (MemoTagPickerEvent) -> Unit,
    onWebPickerEvent: (MemoWebPickerEvent) -> Unit,
    onPlacePickerEvent: (MemoPlacePickerEvent) -> Unit,
    onGeminiEvent: (MemoGeminiDialogEvent) -> Unit,
    onGeminiDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoDetailUiState = { MemoDetailUiState.Loading },
    state: MemoFormState = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    tagUiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
    webUiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    placeCardUiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    geminiUiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
    componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible = { MemoDetailScaffoldComponentVisible() },
    isStandalone: Boolean = true,
) {
    val placeMapState = rememberMemoPlaceMapState(uiState = placeCardUiStateProvider())
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? MemoDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(MemoDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            MemoDetailTopBar(
                uiStateProvider = uiStateProvider,
                onEvent = onEvent,
                componentVisibleProvider = componentVisibleProvider,
                geminiUiStateProvider = geminiUiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            MemoDetailUpdateButton(
                isVisibleProvider = { isChanged },
                uiStateProvider = uiStateProvider,
                onEvent = onEvent,
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        MemoDetailScaffoldContent(
            onEvent = onFormEvent,
            state = state,
            placeMapState = placeMapState,
            isStandalone = isStandalone,
            uiStateProvider = uiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
            webUiStateProvider = webUiStateProvider,
            placeCardUiStateProvider = placeCardUiStateProvider,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        )
    }

    MemoDetailDialogHost(
        onTagPickerEvent = onTagPickerEvent,
        onWebPickerEvent = onWebPickerEvent,
        onPlacePickerEvent = onPlacePickerEvent,
        onGeminiEvent = onGeminiEvent,
        onGeminiDismissRequest = onGeminiDismissRequest,
        state = state,
        tagPagingItems = tagPagingItems,
        tagUiStateProvider = tagUiStateProvider,
        webPagingItems = webPagingItems,
        webUiStateProvider = webUiStateProvider,
        placePagingItems = placePagingItems,
        placeCardUiStateProvider = placeCardUiStateProvider,
        placeCoordinateProvider = { placeMapState?.coordinate?.toCoordinate() },
        geminiUiStateProvider = geminiUiStateProvider,
    )
}

@Composable
private fun MemoDetailDialogHost(
    onTagPickerEvent: (MemoTagPickerEvent) -> Unit,
    onWebPickerEvent: (MemoWebPickerEvent) -> Unit,
    onPlacePickerEvent: (MemoPlacePickerEvent) -> Unit,
    onGeminiEvent: (MemoGeminiDialogEvent) -> Unit,
    onGeminiDismissRequest: () -> Unit,
    state: MemoFormState,
    tagPagingItems: LazyPagingItems<Tag>,
    tagUiStateProvider: () -> MemoTagInputUiState,
    webPagingItems: LazyPagingItems<Web>,
    webUiStateProvider: () -> MemoWebInputUiState,
    placePagingItems: LazyPagingItems<Place>,
    placeCardUiStateProvider: () -> MemoPlaceCardUiState,
    placeCoordinateProvider: () -> Coordinate?,
    geminiUiStateProvider: () -> MemoGeminiUiState,
) {
    MemoTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
    MemoWebPickerDialogHost(dialogState = state.webPickerDialogState, onEvent = onWebPickerEvent, webPagingItems = webPagingItems, uiStateProvider = webUiStateProvider)
    MemoPlacePickerDialogHost(
        dialogState = state.placePickerDialogState,
        onEvent = onPlacePickerEvent,
        uiStateProvider = { placeCardUiStateProvider().placeUiState },
        placePagingItems = placePagingItems,
        coordinateProvider = placeCoordinateProvider,
    )
    MemoGeminiDialogHost(
        onEvent = onGeminiEvent,
        onDismissRequest = onGeminiDismissRequest,
        uiStateProvider = geminiUiStateProvider,
    )
}

private class MemoDetailUiStatePreviewParameter : PreviewParameterProvider<MemoDetailUiState> {
    override val values: Sequence<MemoDetailUiState> =
        sequenceOf(
            MemoDetailUiState.Loading,
            MemoDetailUiState.Content(
                id = Uuid.NIL,
                detail = MemoDetail.EMPTY.copy(title = "메모 제목"),
                isFinished = false,
            ),
        )
}

@ScreenPreview
@Composable
private fun MemoDetailScaffoldPreview(
    @PreviewParameter(MemoDetailUiStatePreviewParameter::class) uiState: MemoDetailUiState,
) {
    val detail = (uiState as? MemoDetailUiState.Content)?.detail ?: MemoDetail.EMPTY

    DiaryTheme {
        MemoDetailScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            onWebPickerEvent = {},
            onPlacePickerEvent = {},
            onGeminiEvent = {},
            onGeminiDismissRequest = {},
            uiStateProvider = { uiState },
            state = rememberMemoDetailFormState(initialDetail = detail),
        )
    }
}
