@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.qr.ui.scan

import kotlinx.browser.document
import org.khronos.webgl.Uint8ClampedArray
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLMediaElement
import org.w3c.dom.HTMLVideoElement
import kotlin.math.max
import kotlin.math.roundToInt

// 해독 시간은 픽셀 수에 비례하고 화면을 채우는 QR은 이 크기로 줄여도 읽히므로 긴 변을 이 길이까지만 둔다.
private const val MAX_FRAME_SIDE_PX = 640

internal class QrFrameDecoder {
    private val canvas = document.createElement("canvas") as HTMLCanvasElement
    private val context = frequentlyReadContext(canvas)

    fun decode(video: HTMLVideoElement): String? {
        val videoWidth = video.videoWidth
        val videoHeight = video.videoHeight

        if (video.readyState < HTMLMediaElement.HAVE_CURRENT_DATA || videoWidth == 0 || videoHeight == 0) return null

        val scale = minOf(1.0, MAX_FRAME_SIDE_PX.toDouble() / max(videoWidth, videoHeight))
        val width = (videoWidth * scale).roundToInt()
        val height = (videoHeight * scale).roundToInt()

        if (canvas.width != width) canvas.width = width
        if (canvas.height != height) canvas.height = height

        context.drawImage(video, 0.0, 0.0, width.toDouble(), height.toDouble())
        val image = context.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())

        return jsQR(data = image.data, width = width, height = height)?.data
    }
}

// 매 프레임 픽셀을 읽어 가므로 브라우저가 캔버스를 GPU 대신 메모리에 두도록 알린다.
@Suppress("UnusedParameter")
private fun frequentlyReadContext(canvas: HTMLCanvasElement): CanvasRenderingContext2D = js("canvas.getContext('2d', { willReadFrequently: true })")

@JsModule("jsqr")
private external fun jsQR(
    data: Uint8ClampedArray,
    width: Int,
    height: Int,
): JsQrCode?

private external interface JsQrCode : JsAny {
    val data: String
}
