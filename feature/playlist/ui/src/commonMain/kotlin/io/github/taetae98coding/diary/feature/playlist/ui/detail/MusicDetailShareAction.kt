package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareResultMessage
import io.github.taetae98coding.diary.compose.core.share.rememberShareResultMessage
import io.github.taetae98coding.diary.compose.core.share.showShareResult
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_link_blank_message
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_link_not_youtube_message
import org.jetbrains.compose.resources.stringResource

@Stable
internal class MusicDetailShareAction(
    private val contentSharer: ContentSharer,
    private val messages: Messages,
) {
    val isSupported: Boolean
        get() = contentSharer.isTextSupported

    suspend fun share(
        state: MusicFormState,
        storedTitle: String,
    ) {
        when (val shareContent = musicShareContentOf(detail = state.detail, storedTitle = storedTitle)) {
            is MusicShareContent.Text ->
                state.snackbarHostState.showShareResult(
                    result = contentSharer.share(content = shareContent.content),
                    message = messages.shareResult,
                )

            is MusicShareContent.LinkBlank -> notifyLinkInvalid(state = state, message = messages.linkBlank)

            is MusicShareContent.LinkNotYoutube -> notifyLinkInvalid(state = state, message = messages.linkNotYoutube)
        }
    }

    private suspend fun notifyLinkInvalid(
        state: MusicFormState,
        message: String,
    ) {
        state.linkState.requestFocus()
        state.snackbarHostState.showImmediate(message = message)
    }

    class Messages(
        val shareResult: ShareResultMessage,
        val linkBlank: String,
        val linkNotYoutube: String,
    )
}

@Composable
internal fun rememberMusicDetailShareAction(contentSharerFactory: ContentSharerFactory): MusicDetailShareAction {
    val contentSharer = contentSharerFactory.rememberContentSharer()
    val messages =
        MusicDetailShareAction.Messages(
            shareResult = rememberShareResultMessage(),
            linkBlank = stringResource(Res.string.music_add_link_blank_message),
            linkNotYoutube = stringResource(Res.string.music_add_link_not_youtube_message),
        )

    return remember(contentSharer, messages.shareResult, messages.linkBlank, messages.linkNotYoutube) {
        MusicDetailShareAction(contentSharer = contentSharer, messages = messages)
    }
}
