@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
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
    override suspend fun toJpeg(uri: FileUri): JpegSource {
        val jpegDataUrl =
            suspendCancellableCoroutine { continuation ->
                encodeToJpegDataUrl(
                    url = uri.value,
                    quality = JPEG_QUALITY_PERCENT / PERCENT,
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
@Suppress("UnusedParameter")
private fun encodeToJpegDataUrl(
    url: String,
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
                    const canvas = document.createElement('canvas');
                    canvas.width = image.naturalWidth;
                    canvas.height = image.naturalHeight;
                    canvas.getContext('2d').drawImage(image, 0, 0);
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
