package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal interface QrScanCameraPreviewContent {
    @Composable
    fun Content(
        onDetect: (String) -> Unit,
        modifier: Modifier,
    )
}
