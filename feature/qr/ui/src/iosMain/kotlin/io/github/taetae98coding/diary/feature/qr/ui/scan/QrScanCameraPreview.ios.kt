@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.awaitCancellation
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceRotationCoordinator
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject

@Composable
internal actual fun QrScanCameraPreview(
    onDetect: (String) -> Unit,
    modifier: Modifier,
) {
    val camera = rememberQrScanCamera()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDetect by rememberUpdatedState(onDetect)
    // 출력은 대리 객체를 약하게 참조하므로 화면이 살아 있는 동안 여기서 붙잡아 둔다.
    val metadataDelegate = remember { QrScanMetadataDelegate(onDetect = { value -> currentOnDetect(value) }) }
    val device = remember { backCameraDevice() }
    val session by produceState<AVCaptureSession?>(initialValue = null, device, metadataDelegate, camera) {
        value = device?.let { camera.open(device = it, metadataDelegate = metadataDelegate) }
    }
    val currentSession = session

    if (currentSession != null) {
        LaunchedEffect(currentSession, lifecycleOwner, camera) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    camera.start(session = currentSession)
                    awaitCancellation()
                } finally {
                    camera.stop(session = currentSession)
                }
            }
        }
    }

    // 미리보기 층은 만들 때 세션을 받으므로, 세션이 구성되면 보기를 새로 만든다.
    key(currentSession) {
        UIKitView(
            factory = { QrScanCameraPreviewView(device = device, session = currentSession) },
            modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor),
        )
    }
}

private fun backCameraDevice(): AVCaptureDevice? =
    AVCaptureDevice.defaultDeviceWithDeviceType(
        deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
        mediaType = AVMediaTypeVideo,
        position = AVCaptureDevicePositionBack,
    )

internal class QrScanMetadataDelegate(
    private val onDetect: (String) -> Unit,
) : NSObject(),
    AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .forEach { code -> onDetect(code.stringValue.orEmpty()) }
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
