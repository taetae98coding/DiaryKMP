package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.qr.ui.qr_scan_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrScanScaffold(
    onEvent: (QrScanScaffoldEvent) -> Unit,
    cameraPreviewContent: QrScanCameraPreviewContent,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.qr_scan_title),
                onNavigateUp = { onEvent(QrScanScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.qr_navigate_up_button_content_description),
            )
        },
    ) { paddingValues ->
        cameraPreviewContent.Content(
            onDetect = { value -> onEvent(QrScanScaffoldEvent.DetectQr(value = value)) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
        )
    }
}

@ScreenPreview
@Composable
private fun QrScanScaffoldPreview() {
    DiaryTheme {
        QrScanScaffold(
            onEvent = {},
            cameraPreviewContent = PreviewQrScanCameraPreviewContent,
        )
    }
}

private object PreviewQrScanCameraPreviewContent : QrScanCameraPreviewContent {
    @Composable
    override fun Content(
        onDetect: (String) -> Unit,
        modifier: Modifier,
    ) {
        Box(modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor))
    }
}
