package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.form.MusicFormState
import io.github.taetae98coding.diary.feature.playlist.ui.form.rememberMusicAddFormState
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_artist_blank_message
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_succeeded_message
import io.github.taetae98coding.diary.feature.playlist.ui.music_add_title_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MusicAddScreenEffect(
    effect: Flow<MusicAddEffect> = emptyFlow(),
    state: MusicFormState = rememberMusicAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.music_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.music_add_title_blank_message)
    val artistBlankMessage = stringResource(Res.string.music_add_artist_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is MusicAddEffect.AddSucceeded -> {
                state.titleState.clearText()
                state.artistState.clearText()
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = addSucceededMessage) }
            }

            is MusicAddEffect.TitleBlank -> {
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = titleBlankMessage) }
            }

            is MusicAddEffect.ArtistBlank -> {
                state.artistState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = artistBlankMessage) }
            }
        }
    }
}
