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
private const val FRAGMENT_SEPARATOR = '#'
private const val CHUNK_BYTES = 1024L * 1024

// 브라우저가 가리키는 파일은 blob URL이라 내용을 한 번에 받아야만 읽을 수 있다. 그래서 읽는 시점에만 메모리에 올린다.
// blob URL 저장소는 fragment를 뺀 주소를 키로 쓰므로, 파일 이름을 담은 fragment는 떼고 브라우저에 넘긴다.
@Factory
internal class WasmFileLocalDataSource : FileLocalDataSource {
    override suspend fun name(uri: FileUri): String {
        val encodedName = uri.value.substringAfter(FRAGMENT_SEPARATOR, missingDelimiterValue = "")

        check(encodedName.isNotEmpty()) { "File uri has no name. uri=$uri" }

        return decodeUriComponent(encoded = encodedName)
    }

    override suspend fun mimeType(uri: FileUri): String =
        suspendCancellableCoroutine { continuation ->
            readBlobType(
                url = uri.blobUrl(),
                onLoad = { type -> continuation.resume(type) },
                onError = { continuation.resumeWithException(uri.unreadableException()) },
            )
        }

    override suspend fun size(uri: FileUri): Long =
        suspendCancellableCoroutine { continuation ->
            readBlobSize(
                url = uri.blobUrl(),
                onLoad = { size -> continuation.resume(size.toLong()) },
                onError = { continuation.resumeWithException(uri.unreadableException()) },
            )
        }

    // 파일 전체를 한 번에 문자열로 바꾸면 원본의 몇 배를 메모리에 올리게 되므로, 조각씩 읽어 이어 붙인다.
    override suspend fun openSource(uri: FileUri): RawSource {
        val size = size(uri = uri)
        val buffer = Buffer()
        var start = 0L

        while (start < size) {
            val end = minOf(start + CHUNK_BYTES, size)
            val dataUrl =
                suspendCancellableCoroutine { continuation ->
                    readBlobSliceAsDataUrl(
                        url = uri.blobUrl(),
                        start = start.toDouble(),
                        end = end.toDouble(),
                        onLoad = { dataUrl -> continuation.resume(dataUrl) },
                        onError = { continuation.resumeWithException(uri.unreadableException()) },
                    )
                }

            buffer.write(Base64.decode(dataUrl.substringAfter(BASE64_SEPARATOR)))
            start = end
        }

        return buffer
    }

    override suspend fun delete(uri: FileUri) {
        revokeObjectUrl(url = uri.blobUrl())
    }

    override suspend fun retain(uri: FileUri): Unit = Unit

    // 고른 파일의 주소는 페이지를 닫을 때까지 파일을 메모리에 붙들어 두므로, 올리기가 끝나면 놓는다.
    override suspend fun release(uri: FileUri) {
        revokeObjectUrl(url = uri.blobUrl())
    }

    // 고른 파일의 주소는 페이지를 닫으면 브라우저가 함께 놓으므로 다음 실행에 남는 것이 없다.
    override suspend fun deleteLeftoverCopies(): Unit = Unit

    private fun FileUri.blobUrl(): String = value.substringBefore(FRAGMENT_SEPARATOR)

    private fun FileUri.unreadableException(): IllegalStateException = IllegalStateException("File cannot be read. uri=$this")
}

@Suppress("UnusedParameter")
private fun decodeUriComponent(encoded: String): String = js("decodeURIComponent(encoded)")

@Suppress("UnusedParameter")
private fun readBlobType(
    url: String,
    onLoad: (String) -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        fetch(url)
            .then((response) => response.blob())
            .then((blob) => onLoad(blob.type))
            .catch(() => onError())
        """,
    )

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
private fun readBlobSliceAsDataUrl(
    url: String,
    start: Double,
    end: Double,
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
                reader.readAsDataURL(blob.slice(start, end));
            })
            .catch(() => onError())
        """,
    )

// blob URL이 아닌 위치는 되돌릴 것이 없어 브라우저가 조용히 무시한다.
@Suppress("UnusedParameter")
private fun revokeObjectUrl(url: String): Unit = js("URL.revokeObjectURL(url)")
