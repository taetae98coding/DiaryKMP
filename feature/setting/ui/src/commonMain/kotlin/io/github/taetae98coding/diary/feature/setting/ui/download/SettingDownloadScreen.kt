package io.github.taetae98coding.diary.feature.setting.ui.download

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.download.form.rememberSettingDownloadFormState

@Composable
internal fun SettingDownloadScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingDownloadScaffoldComponentVisible,
    viewModel: SettingDownloadViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val consumer = uiState as? SettingDownloadUiState.Consumer

    val formState = key(consumer != null) { rememberSettingDownloadFormState(initialSetting = consumer?.setting ?: MusicDownloadProxySetting.EMPTY) }

    SettingDownloadScreenEffect(
        state = formState,
        effect = viewModel.effect,
    )

    SettingDownloadScaffold(
        onEvent = { event ->
            when (event) {
                is SettingDownloadScaffoldEvent.ClickNavigateUp -> navigateUp()
                is SettingDownloadScaffoldEvent.ClickSave -> viewModel.save(setting = formState.setting)
            }
        },
        modifier = modifier,
        state = formState,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
