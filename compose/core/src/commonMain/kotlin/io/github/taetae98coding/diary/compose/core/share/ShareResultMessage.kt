package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.share_copied_message
import io.github.taetae98coding.diary.compose.core.share_failed_message
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import org.jetbrains.compose.resources.stringResource

@Immutable
public class ShareResultMessage(
    private val failedMessage: String,
    private val copiedMessage: String,
) {
    public fun messageOf(result: ShareResult): String? =
        when (result) {
            ShareResult.COMPLETED, ShareResult.DISMISSED -> null
            ShareResult.COPIED -> copiedMessage
            ShareResult.FAILED -> failedMessage
        }
}

@Composable
public fun rememberShareResultMessage(): ShareResultMessage {
    val failedMessage = shareFailedMessage()
    val copiedMessage = shareCopiedMessage()

    return remember(failedMessage, copiedMessage) {
        ShareResultMessage(failedMessage = failedMessage, copiedMessage = copiedMessage)
    }
}

public suspend fun SnackbarHostState.showShareResult(
    result: ShareResult,
    message: ShareResultMessage,
) {
    val text = message.messageOf(result = result) ?: return
    showImmediate(message = text)
}

@Composable
public fun shareFailedMessage(): String = stringResource(Res.string.share_failed_message)

@Composable
public fun shareCopiedMessage(): String = stringResource(Res.string.share_copied_message)
