package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicDetailFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_link_blank_message
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_link_fetch_failed_message
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_link_not_youtube_message
import io.github.taetae98coding.diary.feature.playlist.ui.music_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MusicDetailScreenEffect(
    navigateUp: () -> Unit,
    effect: Flow<MusicDetailEffect> = emptyFlow(),
    state: MusicFormState = rememberMusicDetailFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.music_detail_update_succeeded_message)
    val linkBlankMessage = stringResource(Res.string.music_add_link_blank_message)
    val linkNotYoutubeMessage = stringResource(Res.string.music_add_link_not_youtube_message)
    val linkFetchFailedMessage = stringResource(Res.string.music_add_link_fetch_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is MusicDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { state.hostState.showImmediate(message = updateSucceededMessage) }
            }

            is MusicDetailEffect.LinkBlank -> {
                state.linkState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = linkBlankMessage) }
            }

            is MusicDetailEffect.LinkNotYoutube -> {
                state.linkState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = linkNotYoutubeMessage) }
            }

            is MusicDetailEffect.LinkFetched -> {
                state.fill(
                    link = value.link,
                    title = value.title,
                    artist = value.artist,
                )
            }

            is MusicDetailEffect.LinkFetchFailed -> {
                coroutineScope.launch { state.hostState.showImmediate(message = linkFetchFailedMessage) }
            }

            is MusicDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
