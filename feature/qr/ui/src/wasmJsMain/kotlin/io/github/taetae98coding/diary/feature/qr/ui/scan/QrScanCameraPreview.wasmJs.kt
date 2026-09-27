@file:OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalWasmJsInterop::class,
)

package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.mediacapture.MediaStream
import kotlin.coroutines.resume

private const val FRAME_SCAN_INTERVAL_MILLIS = 100L

@Composable
internal actual fun QrScanCameraPreview(
    onDetect: (String) -> Unit,
    modifier: Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDetect by rememberUpdatedState(onDetect)
    val video = remember { cameraVideoElement() }
    val frameDecoder = remember { QrFrameDecoder() }

    // 탭이 가려지면 생명주기가 멈추므로 그동안은 카메라를 닫아 둔다.
    LaunchedEffect(video, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val stream = openRearCamera() ?: return@repeatOnLifecycle

            try {
                video.srcObject = stream
                playMuted(video)
                while (isActive) {
                    frameDecoder.decode(video)?.let { value -> currentOnDetect(value) }
                    delay(FRAME_SCAN_INTERVAL_MILLIS)
                }
            } finally {
                video.srcObject = null
                stopTracks(stream)
            }
        }
    }

    Box(modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor)) {
        HtmlElementView(
            factory = { video },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun cameraVideoElement(): HTMLVideoElement =
    (document.createElement("video") as HTMLVideoElement).apply {
        muted = true
        autoplay = true
        // Safari는 이 속성이 없으면 영상을 전체 화면 플레이어로 띄운다.
        setAttribute("playsinline", "")
        style.width = "100%"
        style.height = "100%"
        style.setProperty("object-fit", "cover")
        style.backgroundColor = "black"
    }

// 요청이 끝나기 전에 화면을 떠나도 늦게 도착한 스트림을 닫아 카메라가 켜진 채로 남지 않게 한다.
private suspend fun openRearCamera(): MediaStream? =
    suspendCancellableCoroutine { continuation ->
        requestRearCameraStream(
            onStream = { stream ->
                continuation.resume(stream) { _, openedStream, _ -> openedStream?.let(::stopTracks) }
            },
            onFailure = { continuation.resume(null) },
        )
    }

@Suppress("UnusedParameter")
private fun requestRearCameraStream(
    onStream: (MediaStream) -> Unit,
    onFailure: () -> Unit,
): Unit =
    js(
        """
        (() => {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                onFailure();
                return;
            }
            navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false }).then(
                function (stream) { onStream(stream); },
                function () { onFailure(); }
            );
        })()
        """,
    )

@Suppress("UnusedParameter")
private fun playMuted(video: HTMLVideoElement): Unit = js("{ video.play().catch(function () {}); }")

@Suppress("UnusedParameter")
private fun stopTracks(stream: MediaStream): Unit = js("{ stream.getTracks().forEach(function (track) { track.stop(); }); }")
