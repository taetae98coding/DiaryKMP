package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.toCoordinate
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactPickerDialogHost
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactPickerEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoForm
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiDialogEvent
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiDialogHost
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_add_button_content_description
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
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoAddScaffold(
    onEvent: (MemoAddScaffoldEvent) -> Unit,
    onFormEvent: (MemoFormEvent) -> Unit,
    onTagPickerEvent: (MemoTagPickerEvent) -> Unit,
    onWebPickerEvent: (MemoWebPickerEvent) -> Unit,
    onContactPickerEvent: (MemoContactPickerEvent) -> Unit,
    onPlacePickerEvent: (MemoPlacePickerEvent) -> Unit,
    onGeminiEvent: (MemoGeminiDialogEvent) -> Unit,
    onGeminiDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    state: MemoFormState = rememberMemoAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoAddUiState = { MemoAddUiState() },
    tagUiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
    webUiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    contactUiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    placeCardUiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    geminiUiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
    componentVisibleProvider: () -> MemoAddScaffoldComponentVisible = { MemoAddScaffoldComponentVisible() },
    isStandalone: Boolean = true,
) {
    val placeMapState = rememberMemoPlaceMapState(uiState = placeCardUiStateProvider())

    Scaffold(
        modifier = modifier.submitShortcut { onEvent(MemoAddScaffoldEvent.ClickAdd) },
        topBar = {
            MemoAddTopBar(
                onEvent = onEvent,
                componentVisibleProvider = componentVisibleProvider,
                geminiUiStateProvider = geminiUiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(MemoAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.memo_add_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        MemoForm(
            onEvent = onFormEvent,
            state = state,
            placeMapState = placeMapState,
            isStandalone = isStandalone,
            tagUiStateProvider = tagUiStateProvider,
            webUiStateProvider = webUiStateProvider,
            contactUiStateProvider = contactUiStateProvider,
            placeCardUiStateProvider = placeCardUiStateProvider,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        )
    }

    MemoAddDialogHost(
        onTagPickerEvent = onTagPickerEvent,
        onWebPickerEvent = onWebPickerEvent,
        onContactPickerEvent = onContactPickerEvent,
        onPlacePickerEvent = onPlacePickerEvent,
        onGeminiEvent = onGeminiEvent,
        onGeminiDismissRequest = onGeminiDismissRequest,
        state = state,
        tagPagingItems = tagPagingItems,
        tagUiStateProvider = tagUiStateProvider,
        webPagingItems = webPagingItems,
        webUiStateProvider = webUiStateProvider,
        contactPagingItems = contactPagingItems,
        contactUiStateProvider = contactUiStateProvider,
        placePagingItems = placePagingItems,
        placeCardUiStateProvider = placeCardUiStateProvider,
        placeCoordinateProvider = { placeMapState?.coordinate?.toCoordinate() },
        geminiUiStateProvider = geminiUiStateProvider,
    )
}

@Composable
private fun MemoAddDialogHost(
    onTagPickerEvent: (MemoTagPickerEvent) -> Unit,
    onWebPickerEvent: (MemoWebPickerEvent) -> Unit,
    onContactPickerEvent: (MemoContactPickerEvent) -> Unit,
    onPlacePickerEvent: (MemoPlacePickerEvent) -> Unit,
    onGeminiEvent: (MemoGeminiDialogEvent) -> Unit,
    onGeminiDismissRequest: () -> Unit,
    state: MemoFormState,
    tagPagingItems: LazyPagingItems<Tag>,
    tagUiStateProvider: () -> MemoTagInputUiState,
    webPagingItems: LazyPagingItems<Web>,
    webUiStateProvider: () -> MemoWebInputUiState,
    contactPagingItems: LazyPagingItems<Contact>,
    contactUiStateProvider: () -> MemoContactInputUiState,
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
    MemoWebPickerDialogHost(
        dialogState = state.webPickerDialogState,
        onEvent = onWebPickerEvent,
        webPagingItems = webPagingItems,
        uiStateProvider = webUiStateProvider,
    )
    MemoContactPickerDialogHost(
        dialogState = state.contactPickerDialogState,
        onEvent = onContactPickerEvent,
        contactPagingItems = contactPagingItems,
        uiStateProvider = contactUiStateProvider,
    )
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

@ScreenPreview
@Composable
private fun MemoAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        MemoAddScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            onWebPickerEvent = {},
            onContactPickerEvent = {},
            onPlacePickerEvent = {},
            onGeminiEvent = {},
            onGeminiDismissRequest = {},
            uiStateProvider = { MemoAddUiState(isInProgress = isInProgress) },
        )
    }
}
