@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.readByteArray
import org.koin.core.annotation.Factory
import kotlin.io.encoding.Base64

private const val SAVE_MIME_TYPE = "application/octet-stream"

// 브라우저는 앱이 저장 위치를 고르거나 이름이 겹치는지 볼 수 없게 하므로, 받은 내용을 브라우저의 다운로드에 그대로 넘긴다.
@Factory
internal class WasmFileDownloadLocalDataSource : FileDownloadLocalDataSource {
    override val isDirectorySelectable: Boolean = false

    override suspend fun save(
        directory: FileDownloadDirectory?,
        nameCandidate: (index: Int) -> String,
        write: suspend (RawSink) -> Unit,
    ): FileDownloadResult {
        val buffer = Buffer()
        write(buffer)
        downloadBase64(
            base64 = Base64.encode(buffer.readByteArray()),
            name = nameCandidate(0),
            mimeType = SAVE_MIME_TYPE,
        )

        return FileDownloadResult.HandedToBrowser
    }

    override suspend fun retainDirectory(uri: String): FileDownloadDirectory = throw UnsupportedOperationException("Download directory cannot be chosen on the web")

    override suspend fun releaseDirectory(directory: FileDownloadDirectory): Unit = throw UnsupportedOperationException("Download directory cannot be chosen on the web")
}

// 링크를 누르는 동안에만 주소가 필요하지만, 브라우저가 다운로드를 시작하기 전에 놓으면 받지 못하는 브라우저가 있어 잠시 뒤에 놓는다.
@Suppress("UnusedParameter")
private fun downloadBase64(
    base64: String,
    name: String,
    mimeType: String,
): Unit =
    js(
        """
        {
            const binary = atob(base64);
            const bytes = new Uint8Array(binary.length);
            for (let index = 0; index < binary.length; index++) {
                bytes[index] = binary.charCodeAt(index);
            }
            const url = URL.createObjectURL(new Blob([bytes], { type: mimeType }));
            const anchor = document.createElement("a");
            anchor.href = url;
            anchor.download = name;
            anchor.style.display = "none";
            document.body.appendChild(anchor);
            anchor.click();
            anchor.remove();
            setTimeout(() => URL.revokeObjectURL(url), 1000);
        }
        """,
    )
