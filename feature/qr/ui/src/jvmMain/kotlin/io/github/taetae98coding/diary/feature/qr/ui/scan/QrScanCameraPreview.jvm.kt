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
import io.github.taetae98coding.diary.library.avfoundation.AVCaptureQrScanner
import io.github.taetae98coding.diary.library.avfoundation.AVCaptureVideoPreviewPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun QrScanCameraPreview(
    onDetect: (String) -> Unit,
    modifier: Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDetect by rememberUpdatedState(onDetect)
    val previewPanel = remember { AVCaptureVideoPreviewPanel() }

    LaunchedEffect(previewPanel, lifecycleOwner) {
        // 인식 결과는 AppKit 메인 스레드에서 오므로 Compose가 도는 스레드로 옮겨 전달한다.
        val detectedValueChannel = Channel<String>(Channel.BUFFERED)
        // 카메라 장치를 여는 동안과 startRunning·stopRunning이 끝날 때까지 호출한 스레드가 막히므로 메인 스레드 밖에서 부른다.
        val scanner =
            // 만든 세션을 잃지 않도록 취소되어도 결과를 받아 아래 finally에서 해제한다.
            withContext(NonCancellable + Dispatchers.IO) {
                AVCaptureQrScanner.create(onDetect = { value -> detectedValueChannel.trySend(value) })
            } ?: return@LaunchedEffect

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
                        withContext(Dispatchers.IO) { scanner.startRunning() }
                        awaitCancellation()
                    } finally {
                        withContext(NonCancellable + Dispatchers.IO) { scanner.stopRunning() }
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
