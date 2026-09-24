@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import org.koin.core.annotation.Factory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64

private const val BASE64_SEPARATOR = ";base64,"

// 브라우저가 가리키는 파일은 blob URL이라 내용을 한 번에 받아야만 읽을 수 있다. 그래서 읽는 시점에만 메모리에 올린다.
@Factory
internal class WasmFileLocalDataSource : FileLocalDataSource {
    override suspend fun size(uri: FileUri): Long =
        suspendCancellableCoroutine { continuation ->
            readBlobSize(
                url = uri.value,
                onLoad = { size -> continuation.resume(size.toLong()) },
                onError = { continuation.resumeWithException(IllegalStateException("File cannot be read. uri=$uri")) },
            )
        }

    override suspend fun openSource(uri: FileUri): RawSource {
        val dataUrl =
            suspendCancellableCoroutine { continuation ->
                readBlobAsDataUrl(
                    url = uri.value,
                    onLoad = { dataUrl -> continuation.resume(dataUrl) },
                    onError = { continuation.resumeWithException(IllegalStateException("File cannot be read. uri=$uri")) },
                )
            }

        return Buffer().apply { write(Base64.decode(dataUrl.substringAfter(BASE64_SEPARATOR))) }
    }

    override suspend fun delete(uri: FileUri) {
        revokeObjectUrl(url = uri.value)
    }
}

@Suppress("UnusedParameter")
private fun readBlobSize(
    url: String,
    onLoad: (Double) -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        fetch(url)
            .then((response) => response.blob())
            .then((blob) => onLoad(blob.size))
            .catch(() => onError())
        """,
    )

@Suppress("UnusedParameter")
private fun readBlobAsDataUrl(
    url: String,
    onLoad: (String) -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        fetch(url)
            .then((response) => response.blob())
            .then((blob) => {
                const reader = new FileReader();
                reader.onload = () => onLoad(reader.result);
                reader.onerror = () => onError();
                reader.readAsDataURL(blob);
            })
            .catch(() => onError())
        """,
    )

// blob URL이 아닌 위치는 되돌릴 것이 없어 브라우저가 조용히 무시한다.
@Suppress("UnusedParameter")
private fun revokeObjectUrl(url: String): Unit = js("URL.revokeObjectURL(url)")
