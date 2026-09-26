package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.QrScanButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeImage
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_title
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_value_label
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddScaffold(
    onEvent: (QrAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: QrAddScaffoldState = rememberQrAddScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
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
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = DiaryTheme.dimens.screenHorizontalPadding,
                        vertical = DiaryTheme.dimens.screenVerticalPadding,
                    ),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QrCodeImage(valueProvider = { state.valueState.text.toString() })
            OutlinedTextField(
                state = state.valueState,
                modifier =
                    Modifier
                        .widthIn(max = QrAddScaffoldDefaults.ValueInputMaxWidth)
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.qr_add_value_label)) },
                inputTransformation = QrValueInputTransformation,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun QrAddScaffoldPreview() {
    DiaryTheme {
        QrAddScaffold(onEvent = {})
    }
}
