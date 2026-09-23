@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64

private const val BASE64_SEPARATOR = ";base64,"
private const val PERCENT = 100.0

internal class WasmImageConverter : ImageConverter {
    // 브라우저에는 앱이 쓸 파일 시스템이 없어 바꾼 이미지를 메모리에 둔다.
    override suspend fun toJpeg(
        uri: FileUri,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        jpegQuality: Int,
    ): JpegSource {
        val jpegDataUrl =
            suspendCancellableCoroutine { continuation ->
                encodeToJpegDataUrl(
                    url = uri.value,
                    left = cropRegion.left.toDouble(),
                    top = cropRegion.top.toDouble(),
                    right = cropRegion.right.toDouble(),
                    bottom = cropRegion.bottom.toDouble(),
                    maxSideLength = maxSideLength,
                    quality = jpegQuality / PERCENT,
                    onLoad = { dataUrl -> continuation.resume(dataUrl) },
                    onError = { continuation.resumeWithException(IllegalStateException("Image cannot be converted to jpeg. uri=$uri")) },
                )
            }

        return BytesJpegSource(bytes = Base64.decode(jpegDataUrl.substringAfter(BASE64_SEPARATOR)))
    }
}

private class BytesJpegSource(
    private val bytes: ByteArray,
) : JpegSource {
    override val size: Long = bytes.size.toLong()

    override fun openSource(): RawSource = Buffer().apply { write(bytes) }

    override fun close(): Unit = Unit
}

// 브라우저는 자신이 그릴 수 있는 형식만 캔버스에 올리므로, 그리지 못하는 사진은 여기서 실패한다.
// 브라우저가 촬영 방향을 반영해 그리므로 비율 영역은 그 방향의 크기로 화소 좌표로 바꾼다. 화소 계산은 CropGeometry와 같다.
@Suppress("UnusedParameter", "LongParameterList")
private fun encodeToJpegDataUrl(
    url: String,
    left: Double,
    top: Double,
    right: Double,
    bottom: Double,
    maxSideLength: Int,
    quality: Double,
    onLoad: (String) -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        (() => {
            const image = new Image();
            image.onload = function () {
                try {
                    const width = image.naturalWidth;
                    const height = image.naturalHeight;
                    const cropLeft = Math.min(Math.round(left * width), width - 1);
                    const cropTop = Math.min(Math.round(top * height), height - 1);
                    const cropRight = Math.max(Math.min(Math.round(right * width), width), cropLeft + 1);
                    const cropBottom = Math.max(Math.min(Math.round(bottom * height), height), cropTop + 1);
                    const cropWidth = cropRight - cropLeft;
                    const cropHeight = cropBottom - cropTop;
                    const scale = Math.min(1, maxSideLength / Math.max(cropWidth, cropHeight));
                    const canvas = document.createElement('canvas');
                    canvas.width = Math.max(1, Math.round(cropWidth * scale));
                    canvas.height = Math.max(1, Math.round(cropHeight * scale));
                    canvas.getContext('2d').drawImage(image, cropLeft, cropTop, cropWidth, cropHeight, 0, 0, canvas.width, canvas.height);
                    onLoad(canvas.toDataURL('image/jpeg', quality));
                } catch (error) {
                    onError();
                }
            };
            image.onerror = function () { onError(); };
            image.src = url;
        })()
        """,
    )
