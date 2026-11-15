package io.github.taetae98coding.diary.feature.memo.ui.detail.share

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.ShareResultMessage
import io.github.taetae98coding.diary.compose.core.share.rememberShareResultMessage
import io.github.taetae98coding.diary.compose.core.share.showShareResult

@Stable
internal class MemoDetailSharer(
    private val contentSharer: ContentSharer,
    private val snackbarHostState: SnackbarHostState,
    private val resultMessageProvider: () -> ShareResultMessage,
) {
    val support: MemoDetailShareSupport =
        MemoDetailShareSupport(
            isTextSupported = contentSharer.isTextSupported,
            isCalendarFileSupported = contentSharer.isTextSupported && contentSharer.isFileSupported(type = ShareFileType.CALENDAR),
        )

    suspend fun share(content: ShareContent) {
        snackbarHostState.showShareResult(result = contentSharer.share(content = content), message = resultMessageProvider())
    }
}

@Composable
internal fun rememberMemoDetailSharer(
    contentSharerFactory: ContentSharerFactory,
    snackbarHostState: SnackbarHostState,
): MemoDetailSharer {
    val contentSharer = contentSharerFactory.rememberContentSharer()
    val resultMessage by rememberUpdatedState(rememberShareResultMessage())

    return remember(contentSharer, snackbarHostState) {
        MemoDetailSharer(
            contentSharer = contentSharer,
            snackbarHostState = snackbarHostState,
            resultMessageProvider = { resultMessage },
        )
    }
}
