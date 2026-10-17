package io.github.taetae98coding.diary.feature.qr.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.QrScanButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrUndoButton
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.QrActionButton
import io.github.taetae98coding.diary.feature.qr.ui.previewQr
import io.github.taetae98coding.diary.feature.qr.ui.qr_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_button_content_description
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
    TopAppBar(
        // 제목과 액션은 서로 다른 자리이므로 각 슬롯에서 상태를 읽어, 한쪽이 바뀔 때 다른 쪽까지 다시 그리지 않는다.
        title = {
            val uiState = uiStateProvider()

            if (uiState is QrDetailUiState.Content) {
                Text(
                    text = uiState.detail.title,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(QrDetailScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
            )
        },
        actions = {
            val uiState = uiStateProvider()

            if (uiState is QrDetailUiState.Content) {
                actionProvider()?.let { action ->
                    QrActionButton(
                        action = action,
                        onClick = { onEvent(QrDetailScaffoldEvent.ClickAction(action = action)) },
                    )
                }
                QrUndoButton(
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickUndo) },
                    isEnabledProvider = { state.contentState.canUndo },
                )
                QrScanButton(
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickScan) },
                    contentDescription = stringResource(Res.string.qr_scan_button_content_description),
                )
                DeleteButton(
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickDelete) },
                    contentDescription = stringResource(Res.string.qr_detail_delete_button_content_description),
                    isInProgressProvider = { uiState.isDeleteInProgress },
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun QrDetailTopBarPreview() {
    DiaryTheme {
        QrDetailTopBar(
            onEvent = {},
            uiStateProvider = { QrDetailUiState.Content(id = Uuid.NIL, detail = previewQr(title = "회사 출입").detail) },
        )
    }
}
