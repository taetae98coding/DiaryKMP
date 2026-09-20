@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.api.FileReader
import io.github.taetae98coding.diary.core.file.api.FileSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64

private const val DATA_URL_PREFIX = "data:"
private const val BASE64_SEPARATOR = ";base64,"

internal class WasmFileReader : FileReader {
    override suspend fun open(uri: FileUri): FileSource {
        val dataUrl =
            suspendCancellableCoroutine { continuation ->
                readAsDataUrl(
                    url = uri.value,
                    onLoad = { dataUrl -> continuation.resume(dataUrl) },
                    onError = { continuation.resumeWithException(IllegalStateException("File cannot be read. uri=$uri")) },
                )
            }

        check(dataUrl.startsWith(DATA_URL_PREFIX) && dataUrl.contains(BASE64_SEPARATOR)) { "File cannot be read. uri=$uri" }

        return WasmFileSource(
            mimeType = dataUrl.substringAfter(DATA_URL_PREFIX).substringBefore(BASE64_SEPARATOR),
            bytes = Base64.decode(dataUrl.substringAfter(BASE64_SEPARATOR)),
        )
    }
}

private class WasmFileSource(
    override val mimeType: String,
    private val bytes: ByteArray,
) : FileSource {
    override val size: Long = bytes.size.toLong()

    override fun openSource(): RawSource = Buffer().apply { write(bytes) }
}

@Suppress("UnusedParameter")
private fun readAsDataUrl(
    url: String,
    onLoad: (String) -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        (() => {
            fetch(url)
                .then(function (response) { return response.blob(); })
                .then(function (blob) {
                    const reader = new FileReader();
                    reader.onload = function () { onLoad(reader.result); };
                    reader.onerror = function () { onError(); };
                    reader.readAsDataURL(blob);
                })
                .catch(function () { onError(); });
        })()
        """,
    )
