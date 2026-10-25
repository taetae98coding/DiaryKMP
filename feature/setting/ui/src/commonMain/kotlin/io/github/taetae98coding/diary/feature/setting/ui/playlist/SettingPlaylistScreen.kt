package io.github.taetae98coding.diary.feature.setting.ui.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.playlist.form.rememberSettingPlaylistFormState

@Composable
internal fun SettingPlaylistScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingPlaylistScaffoldComponentVisible,
    viewModel: SettingPlaylistViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val consumer = uiState as? SettingPlaylistUiState.Consumer

    val formState = key(consumer != null) { rememberSettingPlaylistFormState(initialSetting = consumer?.setting ?: MusicDownloadProxySetting.EMPTY) }

    SettingPlaylistScreenEffect(
        state = formState,
        effect = viewModel.effect,
    )

    SettingPlaylistScaffold(
        onEvent = { event ->
            when (event) {
                is SettingPlaylistScaffoldEvent.ClickNavigateUp -> navigateUp()
                is SettingPlaylistScaffoldEvent.ClickSave -> viewModel.save(setting = formState.setting)
            }
        },
        modifier = modifier,
        state = formState,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
