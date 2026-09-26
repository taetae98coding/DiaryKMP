package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.button.QrScanButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_title
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddScaffold(
    onEvent: (QrAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
    uiStateProvider: () -> QrAddUiState = { QrAddUiState() },
) {
    Scaffold(
        modifier = modifier.submitShortcut { onEvent(QrAddScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.qr_add_title),
                onNavigateUp = { onEvent(QrAddScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
                actions = {
                    QrScanButton(
                        onClick = { onEvent(QrAddScaffoldEvent.ClickScan) },
                        contentDescription = stringResource(Res.string.qr_scan_button_content_description),
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
        )
    }
}

@ScreenPreview
@Composable
private fun QrAddScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        QrAddScaffold(
            onEvent = {},
            uiStateProvider = { QrAddUiState(isInProgress = isInProgress) },
        )
    }
}
