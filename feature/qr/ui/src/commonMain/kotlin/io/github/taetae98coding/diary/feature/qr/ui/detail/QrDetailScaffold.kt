package io.github.taetae98coding.diary.feature.qr.ui.detail

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
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddForm
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.previewQr
import io.github.taetae98coding.diary.feature.qr.ui.qr_detail_loading_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_detail_update_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun QrDetailScaffold(
    onEvent: (QrDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
    uiStateProvider: () -> QrDetailUiState = { QrDetailUiState.Loading },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? QrDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }

    Scaffold(
        modifier = modifier.submitShortcut(isEnabledProvider = { isChanged }) { onEvent(QrDetailScaffoldEvent.ClickUpdate) },
        topBar = {
            QrDetailTopBar(
                onEvent = onEvent,
                state = state,
                uiStateProvider = uiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(QrDetailScaffoldEvent.ClickUpdate) },
                    contentDescription = stringResource(Res.string.qr_detail_update_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? QrDetailUiState.Content)?.isUpdateInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        DiaryCrossfade(
            targetState = uiStateProvider() is QrDetailUiState.Content,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) { isContent ->
            if (isContent) {
                QrAddForm(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    isMapDisplayedProvider = { (uiStateProvider() as? QrDetailUiState.Content)?.isMapDisplayed == true },
                )
            } else {
                DiaryLoadingBox(
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = stringResource(Res.string.qr_detail_loading_content_description),
                )
            }
        }
    }
}

private class QrDetailUiStatePreviewParameter : PreviewParameterProvider<QrDetailUiState> {
    override val values: Sequence<QrDetailUiState> =
        sequenceOf(
            QrDetailUiState.Loading,
            QrDetailUiState.Content(id = Uuid.NIL, detail = previewQr(title = "회사 출입").detail),
        )
}

@ScreenPreview
@Composable
private fun QrDetailScaffoldPreview(
    @PreviewParameter(QrDetailUiStatePreviewParameter::class) uiState: QrDetailUiState,
) {
    val detail = (uiState as? QrDetailUiState.Content)?.detail ?: QrDetail.EMPTY

    DiaryTheme {
        QrDetailScaffold(
            onEvent = {},
            state = rememberQrAddFormState(initialDetail = detail),
            uiStateProvider = { uiState },
        )
    }
}
