package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal expect fun QrScanCameraPreview(
    onDetect: (String) -> Unit,
    modifier: Modifier = Modifier,
)

@ComponentPreview
@Composable
private fun QrScanCameraPreviewPreview() {
    DiaryTheme {
        QrScanCameraPreview(onDetect = {})
    }
}
