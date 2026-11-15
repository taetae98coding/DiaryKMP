package io.github.taetae98coding.diary.feature.qr.ui.detail.share

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.compose.core.share.shareFailedMessage
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate

@Stable
internal class QrShareController(
    private val sharer: ContentSharer,
    private val encoder: QrCodePngEncoder,
    private val failedMessage: String,
) {
    val isSupported: Boolean
        get() = sharer.isFileSupported(type = ShareFileType.PNG)

    suspend fun share(
        title: String,
        qrValue: String,
        snackbarHostState: SnackbarHostState,
    ) {
        // QR에 담을 수 없는 값이면 인코더가 IllegalArgumentException을, PNG로 만들지 못하면 IllegalStateException을 던진다.
        val result =
            try {
                val bytes = encoder.encode(value = qrValue)
                sharer.share(content = ShareContent.File(bytes = bytes, name = title, type = ShareFileType.PNG, text = title, title = title))
            } catch (_: IllegalArgumentException) {
                ShareResult.FAILED
            } catch (_: IllegalStateException) {
                ShareResult.FAILED
            }

        if (result == ShareResult.FAILED) {
            snackbarHostState.showImmediate(message = failedMessage)
        }
    }
}

@Composable
internal fun rememberQrShareController(
    sharerFactory: ContentSharerFactory,
    encoder: QrCodePngEncoder,
): QrShareController {
    val sharer = sharerFactory.rememberContentSharer()
    val failedMessage = shareFailedMessage()

    return remember(sharer, encoder, failedMessage) { QrShareController(sharer = sharer, encoder = encoder, failedMessage = failedMessage) }
}
