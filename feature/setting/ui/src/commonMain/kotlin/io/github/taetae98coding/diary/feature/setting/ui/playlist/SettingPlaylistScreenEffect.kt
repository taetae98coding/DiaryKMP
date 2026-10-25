package io.github.taetae98coding.diary.feature.setting.ui.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.playlist.form.SettingPlaylistFormState
import io.github.taetae98coding.diary.feature.setting.ui.setting_playlist_save_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_playlist_save_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingPlaylistScreenEffect(
    state: SettingPlaylistFormState,
    effect: Flow<SettingPlaylistEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val saveSucceededMessage = stringResource(Res.string.setting_playlist_save_succeeded_message)
    val saveFailedMessage = stringResource(Res.string.setting_playlist_save_failed_message)

    CollectEffect(effect) { value ->
        val message =
            when (value) {
                is SettingPlaylistEffect.SaveSucceeded -> saveSucceededMessage
                is SettingPlaylistEffect.SaveFailed -> saveFailedMessage
            }

        coroutineScope.launch { state.snackbarHostState.showImmediate(message = message) }
    }
}
