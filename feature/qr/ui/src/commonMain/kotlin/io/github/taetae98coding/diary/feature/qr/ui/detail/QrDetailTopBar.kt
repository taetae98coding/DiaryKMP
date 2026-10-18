package io.github.taetae98coding.diary.feature.qr.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.contentDescription
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.topBarIcon
import io.github.taetae98coding.diary.feature.qr.ui.previewQr
import io.github.taetae98coding.diary.feature.qr.ui.previewQrActionList
import io.github.taetae98coding.diary.feature.qr.ui.qr_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_undo_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun QrDetailTopBar(
    onEvent: (QrDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
    uiStateProvider: () -> QrDetailUiState = { QrDetailUiState.Loading },
    actionProvider: () -> QrAction? = { null },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description)
    val undoContentDescription = stringResource(Res.string.qr_undo_button_content_description)
    val scanContentDescription = stringResource(Res.string.qr_scan_button_content_description)
    val deleteContentDescription = stringResource(Res.string.qr_detail_delete_button_content_description)
    val action = actionProvider()
    val actionContentDescription = action?.let { stringResource(it.contentDescription) }

    DiaryTopBar(
        title = {
            if (uiState is QrDetailUiState.Content) {
                Title(text = uiState.detail.title, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(QrDetailScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
        actions = {
            if (uiState is QrDetailUiState.Content) {
                if (action != null && actionContentDescription != null) {
                    IconAction(
                        icon = action.topBarIcon,
                        contentDescription = actionContentDescription,
                        onClick = { onEvent(QrDetailScaffoldEvent.ClickAction(action = action)) },
                    )
                }
                IconAction(
                    icon = DiaryTopBarIcon.UNDO,
                    contentDescription = undoContentDescription,
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickUndo) },
                    isEnabled = state.contentState.canUndo,
                )
                IconAction(
                    icon = DiaryTopBarIcon.QR_SCAN,
                    contentDescription = scanContentDescription,
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickScan) },
                )
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

private class QrActionPreviewParameter : PreviewParameterProvider<QrAction> {
    override val values: Sequence<QrAction> = previewQrActionList().asSequence()
}

@ComponentPreview
@Composable
private fun QrDetailTopBarPreview(
    @PreviewParameter(QrActionPreviewParameter::class) action: QrAction,
) {
    DiaryTheme {
        QrDetailTopBar(
            onEvent = {},
            uiStateProvider = { QrDetailUiState.Content(id = Uuid.NIL, detail = previewQr(title = "회사 출입").detail) },
            actionProvider = { action },
        )
    }
}
