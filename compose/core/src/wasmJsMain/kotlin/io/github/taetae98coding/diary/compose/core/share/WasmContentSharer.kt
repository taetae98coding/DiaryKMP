@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64

public object WasmContentSharerFactory : ContentSharerFactory {
    @Composable
    override fun rememberContentSharer(): ContentSharer = remember { WasmContentSharer() }
}

internal class WasmContentSharer : ContentSharer {
    private val isTextShareSheetSupported: Boolean = canShareText()

    override val isTextSupported: Boolean = isTextShareSheetSupported || canWriteClipboardText()

    override fun isFileSupported(type: ShareFileType): Boolean = canShareFile(fileName = type.fileNameOf(name = ""), mimeType = type.mimeType)

    override suspend fun share(content: ShareContent): ShareResult =
        if (content is ShareContent.Text && !isTextShareSheetSupported) {
            copy(text = content.text)
        } else {
            shareWithSheet(content = content)
        }

    // 클립보드 쓰기도 사용자 조작 직후에만 허락되므로 누른 즉시 부른다.
    private suspend fun copy(text: String): ShareResult =
        suspendCancellableCoroutine { continuation ->
            writeClipboardText(
                text = text,
                onComplete = { if (continuation.isActive) continuation.resume(ShareResult.COPIED) },
                onError = { if (continuation.isActive) continuation.resume(ShareResult.FAILED) },
            )
        }

    private suspend fun shareWithSheet(content: ShareContent): ShareResult =
        suspendCancellableCoroutine { continuation ->
            val file = content as? ShareContent.File

            shareWithNavigator(
                title = content.title,
                text =
                    when (content) {
                        is ShareContent.Text -> content.text
                        is ShareContent.File -> content.text
                    },
                fileBase64 = file?.let { Base64.encode(it.bytes) }.orEmpty(),
                fileName = file?.fileName.orEmpty(),
                mimeType = file?.type?.mimeType.orEmpty(),
                onComplete = { if (continuation.isActive) continuation.resume(ShareResult.COMPLETED) },
                onAbort = { if (continuation.isActive) continuation.resume(ShareResult.DISMISSED) },
                onError = { if (continuation.isActive) continuation.resume(ShareResult.FAILED) },
            )
        }
}

private fun canShareText(): Boolean =
    js(
        """
        (() => {
            if (typeof navigator.share !== 'function') {
                return false;
            }
            if (typeof navigator.canShare !== 'function') {
                return true;
            }
            try {
                return navigator.canShare({ text: 'share' });
            } catch (error) {
                return false;
            }
        })()
        """,
    )

// 클립보드는 HTTPS처럼 안전한 출처에서만 열린다.
private fun canWriteClipboardText(): Boolean =
    js(
        """
        (() => typeof navigator.clipboard === 'object' && navigator.clipboard !== null && typeof navigator.clipboard.writeText === 'function')()
        """,
    )

// 사용자가 권한을 거절했거나 문서에 초점이 없으면 거절된다.
@Suppress("UnusedParameter")
private fun writeClipboardText(
    text: String,
    onComplete: () -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        (() => {
            try {
                navigator.clipboard.writeText(text).then(
                    () => onComplete(),
                    () => onError(),
                );
            } catch (error) {
                onError();
            }
        })()
        """,
    )

// 브라우저는 파일 공유를 형식마다 따로 허용하므로(Chrome은 연락처·일정 파일을 받지 않는다) 그 형식의 빈 파일로 물어본다.
@Suppress("UnusedParameter")
private fun canShareFile(
    fileName: String,
    mimeType: String,
): Boolean =
    js(
        """
        (() => {
            if (typeof navigator.share !== 'function' || typeof navigator.canShare !== 'function') {
                return false;
            }
            try {
                return navigator.canShare({ files: [new File([''], fileName, { type: mimeType })] });
            } catch (error) {
                return false;
            }
        })()
        """,
    )

// 사용자가 공유 시트를 닫으면 AbortError로, 사용자 조작 없이 부르거나 형식을 받지 않으면 그 밖의 오류로 거절된다.
@Suppress("UnusedParameter", "LongParameterList")
private fun shareWithNavigator(
    title: String,
    text: String,
    fileBase64: String,
    fileName: String,
    mimeType: String,
    onComplete: () -> Unit,
    onAbort: () -> Unit,
    onError: () -> Unit,
): Unit =
    js(
        """
        (() => {
            if (typeof navigator.share !== 'function') {
                onError();
                return;
            }
            const data = {};
            if (title) {
                data.title = title;
            }
            if (text) {
                data.text = text;
            }
            if (fileName) {
                const binary = atob(fileBase64);
                const bytes = new Uint8Array(binary.length);
                for (let index = 0; index < binary.length; index++) {
                    bytes[index] = binary.charCodeAt(index);
                }
                data.files = [new File([bytes], fileName, { type: mimeType })];
            }
            try {
                navigator.share(data).then(
                    () => onComplete(),
                    (error) => {
                        if (error && error.name === 'AbortError') {
                            onAbort();
                        } else {
                            onError();
                        }
                    },
                );
            } catch (error) {
                onError();
            }
        })()
        """,
    )
