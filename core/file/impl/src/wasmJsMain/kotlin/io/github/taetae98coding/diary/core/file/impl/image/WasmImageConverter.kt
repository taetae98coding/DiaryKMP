@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.file.impl.image

import io.github.taetae98coding.diary.core.file.api.ImageConverter
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Factory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val PERCENT = 100.0

// 브라우저에는 앱이 쓸 파일 시스템이 없어 바꾼 이미지를 blob URL로 가리킨다. 그 위치를 읽고 지우는 것은 WasmFileLocalDataSource가 맡는다.
@Factory
internal class WasmImageConverter : ImageConverter {
    override suspend fun convert(
        source: FileUri,
        format: ImageFormat,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        quality: Int,
    ): FileUri {
        val blobUrl =
            suspendCancellableCoroutine { continuation ->
                encodeToBlobUrl(
                    url = source.value,
                    left = cropRegion.left.toDouble(),
                    top = cropRegion.top.toDouble(),
                    right = cropRegion.right.toDouble(),
                    bottom = cropRegion.bottom.toDouble(),
                    maxSideLength = maxSideLength,
                    mimeType = format.toMimeType(),
                    quality = quality / PERCENT,
                    onLoad = { url -> continuation.resume(url) },
                    onError = { continuation.resumeWithException(IllegalStateException("Image cannot be converted. uri=$source, format=$format")) },
                )
            }

        return FileUri(blobUrl)
    }

    private fun ImageFormat.toMimeType(): String =
        when (this) {
            ImageFormat.JPEG -> "image/jpeg"
        }
}

// 브라우저는 자신이 그릴 수 있는 형식만 캔버스에 올리므로, 그리지 못하는 사진은 여기서 실패한다.
// 브라우저가 촬영 방향을 반영해 그리므로 비율 영역은 그 방향의 크기로 화소 좌표로 바꾼다. 화소 계산은 CropGeometry와 같다.
@Suppress("UnusedParameter", "LongParameterList")
private fun encodeToBlobUrl(
    url: String,
    left: Double,
    top: Double,
    right: Double,
    bottom: Double,
    maxSideLength: Int,
    mimeType: String,
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
                    canvas.toBlob(function (blob) {
                        if (blob) {
                            onLoad(URL.createObjectURL(blob));
                        } else {
                            onError();
                        }
                    }, mimeType, quality);
                } catch (error) {
                    onError();
                }
            };
            image.onerror = function () { onError(); };
            image.src = url;
        })()
        """,
    )
