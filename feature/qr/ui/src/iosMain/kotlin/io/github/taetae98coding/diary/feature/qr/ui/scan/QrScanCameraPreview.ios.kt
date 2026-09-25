@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceRotationCoordinator
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
import platform.UIKit.UIView

@Composable
internal actual fun QrScanCameraPreview(modifier: Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val device = remember { backCameraDevice() }
    val session = remember(device) { device?.let(::captureSession) }

    if (session != null) {
        // startRunning과 stopRunning은 끝날 때까지 호출한 스레드를 막으므로 메인 스레드 밖에서 부른다.
        LaunchedEffect(session, lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    withContext(Dispatchers.IO) { session.startRunning() }
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable + Dispatchers.IO) { session.stopRunning() }
                }
            }
        }
    }

    UIKitView(
        factory = { QrScanCameraPreviewView(device = device, session = session) },
        modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor),
    )
}

private fun backCameraDevice(): AVCaptureDevice? =
    AVCaptureDevice.defaultDeviceWithDeviceType(
        deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
        mediaType = AVMediaTypeVideo,
        position = AVCaptureDevicePositionBack,
    )

private fun captureSession(device: AVCaptureDevice): AVCaptureSession? {
    val input = AVCaptureDeviceInput.deviceInputWithDevice(device = device, error = null)
    val session = AVCaptureSession()

    return if (input != null && session.canAddInput(input)) {
        session.addInput(input)
        session
    } else {
        null
    }
}

private class QrScanCameraPreviewView(
    device: AVCaptureDevice?,
    session: AVCaptureSession?,
) : UIView(frame = CGRectZero.readValue()) {
    private val previewLayer = session?.let { AVCaptureVideoPreviewLayer(session = it) }
    private val rotationCoordinator =
        if (device != null && previewLayer != null) {
            AVCaptureDeviceRotationCoordinator(device = device, previewLayer = previewLayer)
        } else {
            null
        }

    init {
        backgroundColor = UIColor.blackColor
        previewLayer?.let { layer ->
            layer.videoGravity = AVLayerVideoGravityResizeAspectFill
            this.layer.addSublayer(layer)
        }
    }

    // 기기를 돌리면 크기가 바뀌어 다시 배치되므로, 이때 미리보기 크기와 함께 영상의 회전도 맞춘다.
    override fun layoutSubviews() {
        super.layoutSubviews()

        val layer = previewLayer ?: return
        layer.frame = bounds
        rotationCoordinator?.let { coordinator ->
            layer.connection?.videoRotationAngle = coordinator.videoRotationAngleForHorizonLevelPreview
        }
    }
}
