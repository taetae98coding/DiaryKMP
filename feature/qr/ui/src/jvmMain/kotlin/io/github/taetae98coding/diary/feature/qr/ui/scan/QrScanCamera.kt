package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.library.avfoundation.AVCaptureQrScanner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
internal fun rememberQrScanCamera(): QrScanCamera {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<QrScanCameraDispatcher>())

    return remember(dispatcher) { QrScanCamera(dispatcher = dispatcher) }
}

// 카메라 장치를 여는 동안과 startRunning·stopRunning이 끝날 때까지 호출한 스레드가 막히므로 메인 스레드 밖에서 한다.
internal class QrScanCamera(
    private val dispatcher: CoroutineDispatcher,
) {
    // 만든 세션을 잃지 않도록 취소되어도 결과를 받아 부른 쪽이 해제하게 한다.
    suspend fun open(onDetect: (String) -> Unit): AVCaptureQrScanner? = withContext(NonCancellable + dispatcher) { AVCaptureQrScanner.create(onDetect = onDetect) }

    suspend fun start(scanner: AVCaptureQrScanner) {
        withContext(dispatcher) { scanner.startRunning() }
    }

    // 화면을 떠나며 취소되어도 카메라는 꺼야 하므로 취소 없이 끝까지 멈춘다.
    suspend fun stop(scanner: AVCaptureQrScanner) {
        withContext(NonCancellable + dispatcher) { scanner.stopRunning() }
    }
}
