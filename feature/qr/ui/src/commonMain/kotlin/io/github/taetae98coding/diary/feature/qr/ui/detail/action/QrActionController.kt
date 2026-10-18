package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.domain.qr.content.qrActionOf
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_action_failed_message
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@Stable
internal class QrActionController(
    private val launcher: QrActionLauncher,
    private val failedMessage: String,
) {
    fun actionOf(qrValue: String): QrAction? = qrActionOf(raw = qrValue, timeZone = TimeZone.currentSystemDefault())?.takeIf(launcher::isSupported)

    suspend fun launch(
        action: QrAction,
        snackbarHostState: SnackbarHostState,
    ) {
        if (!launcher.launch(action = action)) {
            snackbarHostState.showImmediate(message = failedMessage)
        }
    }
}

@Composable
internal fun rememberQrActionController(): QrActionController {
    val launcher = rememberQrActionLauncher()
    val failedMessage = stringResource(Res.string.qr_action_failed_message)

    return remember(launcher, failedMessage) { QrActionController(launcher = launcher, failedMessage = failedMessage) }
}
