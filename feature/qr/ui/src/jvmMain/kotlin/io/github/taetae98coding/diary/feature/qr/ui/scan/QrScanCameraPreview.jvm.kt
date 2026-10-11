package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.taetae98coding.diary.library.avfoundation.AVCaptureVideoPreviewPanel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal actual fun QrScanCameraPreview(
    onDetect: (String) -> Unit,
    modifier: Modifier,
) {
    val camera = rememberQrScanCamera()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDetect by rememberUpdatedState(onDetect)
    val previewPanel = remember { AVCaptureVideoPreviewPanel() }

    LaunchedEffect(previewPanel, lifecycleOwner, camera) {
        // 인식 결과는 AppKit 메인 스레드에서 오므로 Compose가 도는 스레드로 옮겨 전달한다.
        val detectedValueChannel = Channel<String>(Channel.BUFFERED)
        val scanner = camera.open(onDetect = { value -> detectedValueChannel.trySend(value) }) ?: return@LaunchedEffect

        try {
            previewPanel.scanner = scanner
            coroutineScope {
                launch {
                    for (value in detectedValueChannel) {
                        currentOnDetect(value)
                    }
                }

                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        camera.start(scanner = scanner)
                        awaitCancellation()
                    } finally {
                        camera.stop(scanner = scanner)
                    }
                }
            }
        } finally {
            previewPanel.scanner = null
            scanner.close()
        }
    }

    Box(modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor)) {
        SwingPanel(
            factory = { previewPanel },
            modifier = Modifier.fillMaxSize(),
            background = QrScanCameraPreviewDefaults.EmptyColor,
        )
    }
}
