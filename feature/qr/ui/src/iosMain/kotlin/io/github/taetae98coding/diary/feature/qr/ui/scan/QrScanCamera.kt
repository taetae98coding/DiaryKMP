@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.darwin.dispatch_get_main_queue

@Composable
internal fun rememberQrScanCamera(): QrScanCamera {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<QrScanCameraDispatcher>())

    return remember(dispatcher) { QrScanCamera(dispatcher = dispatcher) }
}

// 입력을 붙이며 카메라 장치를 열 때와 startRunning·stopRunning이 끝날 때까지 호출한 스레드가 막히므로 메인 스레드 밖에서 한다.
internal class QrScanCamera(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun open(
        device: AVCaptureDevice,
        metadataDelegate: QrScanMetadataDelegate,
    ): AVCaptureSession? = withContext(dispatcher) { captureSession(device = device, metadataDelegate = metadataDelegate) }

    suspend fun start(session: AVCaptureSession) {
        withContext(dispatcher) { session.startRunning() }
    }

    // 화면을 떠나며 취소되어도 카메라는 꺼야 하므로 취소 없이 끝까지 멈춘다.
    suspend fun stop(session: AVCaptureSession) {
        withContext(NonCancellable + dispatcher) { session.stopRunning() }
    }

    private fun captureSession(
        device: AVCaptureDevice,
        metadataDelegate: QrScanMetadataDelegate,
    ): AVCaptureSession? {
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device = device, error = null)
        val session = AVCaptureSession()

        if (input == null || !session.canAddInput(input)) return null

        session.addInput(input)

        val output = AVCaptureMetadataOutput()
        if (session.canAddOutput(output)) {
            session.addOutput(output)
            // 인식할 코드 종류는 출력을 세션에 붙인 뒤에야 고를 수 있다.
            output.setMetadataObjectsDelegate(metadataDelegate, queue = dispatch_get_main_queue())
            output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        }

        return session
    }
}
