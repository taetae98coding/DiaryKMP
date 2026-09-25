package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingQrScanButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_title
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrHomeScaffold(
    onEvent: (QrHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.qr_home_title),
                onNavigateUp = { onEvent(QrHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = hostState) },
        floatingActionButton = {
            FloatingQrScanButton(
                onClick = { onEvent(QrHomeScaffoldEvent.ClickScan) },
                contentDescription = stringResource(Res.string.qr_scan_button_content_description),
            )
        },
    ) { _ -> }
}

@ScreenPreview
@Composable
private fun QrHomeScaffoldPreview() {
    DiaryTheme {
        QrHomeScaffold(onEvent = {})
    }
}
