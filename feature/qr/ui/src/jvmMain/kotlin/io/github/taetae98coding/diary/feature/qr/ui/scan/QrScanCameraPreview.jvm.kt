package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun QrScanCameraPreview(modifier: Modifier) {
    Box(modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor))
}
