package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ShareResultMessage
import io.github.taetae98coding.diary.compose.core.share.rememberShareResultMessage
import io.github.taetae98coding.diary.compose.core.share.showShareResult
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.core.uri.tryOpenUri
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_detail_open_failed_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun rememberWebDetailOutsideAction(
    contentSharer: ContentSharer,
    snackbarHostState: SnackbarHostState,
): WebDetailOutsideAction {
    val uriHandler = LocalUriHandler.current
    val openFailedMessage = stringResource(Res.string.web_detail_open_failed_message)
    val shareResultMessage = rememberShareResultMessage()

    return remember(uriHandler, contentSharer, snackbarHostState, openFailedMessage, shareResultMessage) {
        WebDetailOutsideAction(
            uriHandler = uriHandler,
            contentSharer = contentSharer,
            snackbarHostState = snackbarHostState,
            openFailedMessage = openFailedMessage,
            shareResultMessage = shareResultMessage,
        )
    }
}

internal class WebDetailOutsideAction(
    private val uriHandler: UriHandler,
    private val contentSharer: ContentSharer,
    private val snackbarHostState: SnackbarHostState,
    private val openFailedMessage: String,
    private val shareResultMessage: ShareResultMessage,
) {
    suspend fun openInNew(url: String?) {
        if (url != null && !uriHandler.tryOpenUri(url)) {
            snackbarHostState.showImmediate(message = openFailedMessage)
        }
    }

    suspend fun share(
        input: WebDetail,
        saved: WebDetail?,
    ) {
        if (saved != null) {
            snackbarHostState.showShareResult(
                result = contentSharer.share(content = webShareContentOf(input = input, saved = saved)),
                message = shareResultMessage,
            )
        }
    }
}
