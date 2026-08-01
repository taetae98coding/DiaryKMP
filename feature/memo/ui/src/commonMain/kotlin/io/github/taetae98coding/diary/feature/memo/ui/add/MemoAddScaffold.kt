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
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoForm
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormEvent
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
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
    onPlacePickerEvent: (MemoPlacePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoFormState = rememberMemoAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoAddUiState = { MemoAddUiState() },
    tagUiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
    webUiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    placeCardUiStateProvider: () -> MemoPlaceCardUiState = { MemoPlaceCardUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
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
            placeCardUiStateProvider = placeCardUiStateProvider,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        )
    }

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
    MemoPlacePickerDialogHost(
        dialogState = state.placePickerDialogState,
        onEvent = onPlacePickerEvent,
        uiStateProvider = { placeCardUiStateProvider().placeUiState },
        placePagingItems = placePagingItems,
        coordinateProvider = { placeMapState?.coordinate?.toCoordinate() },
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
            onPlacePickerEvent = {},
            uiStateProvider = { MemoAddUiState(isInProgress = isInProgress) },
        )
    }
}
