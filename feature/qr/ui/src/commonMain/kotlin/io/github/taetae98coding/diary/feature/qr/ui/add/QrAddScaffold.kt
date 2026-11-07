package io.github.taetae98coding.diary.feature.qr.ui.add

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
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddForm
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrFormEvent
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_title
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_undo_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddScaffold(
    onEvent: (QrAddScaffoldEvent) -> Unit,
    onFormEvent: (QrFormEvent) -> Unit,
    onTagPickerEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> QrAddUiState = { QrAddUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    componentVisibleProvider: () -> QrAddScaffoldComponentVisible = { QrAddScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(QrAddScaffoldEvent.ClickAdd) },
        topBar = {
            val undoContentDescription = stringResource(Res.string.qr_undo_button_content_description)
            val scanContentDescription = stringResource(Res.string.qr_scan_button_content_description)

            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.qr_add_title),
                onNavigateUp = { onEvent(QrAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
                actions = {
                    IconAction(
                        icon = DiaryTopBarIcon.UNDO,
                        contentDescription = undoContentDescription,
                        onClick = { onEvent(QrAddScaffoldEvent.ClickUndo) },
                        isEnabled = state.contentState.canUndo,
                    )
                    IconAction(
                        icon = DiaryTopBarIcon.QR_SCAN,
                        contentDescription = scanContentDescription,
                        onClick = { onEvent(QrAddScaffoldEvent.ClickScan) },
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(QrAddScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.qr_add_button_content_description),
                isInProgressProvider = { uiStateProvider().isInProgress },
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        QrAddForm(
            onEvent = onFormEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            isMapDisplayedProvider = { uiStateProvider().isMapDisplayed },
            tagUiStateProvider = tagUiStateProvider,
        )
    }

    EntityTagPickerDialogHost(
        dialogState = state.tagPickerDialogState,
        onEvent = onTagPickerEvent,
        tagPagingItems = tagPagingItems,
        uiStateProvider = tagUiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun QrAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        QrAddScaffold(
            onEvent = {},
            onFormEvent = {},
            onTagPickerEvent = {},
            uiStateProvider = { QrAddUiState(isInProgress = isInProgress) },
        )
    }
}
