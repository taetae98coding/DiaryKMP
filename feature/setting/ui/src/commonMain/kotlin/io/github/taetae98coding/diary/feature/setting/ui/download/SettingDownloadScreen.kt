package io.github.taetae98coding.diary.feature.setting.ui.download

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.download.form.SettingDownloadFormState
import io.github.taetae98coding.diary.feature.setting.ui.download.form.rememberSettingDownloadFormState
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_save_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_save_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

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

    SaveEffect(
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

@Composable
private fun SaveEffect(
    state: SettingDownloadFormState,
    effect: Flow<SettingDownloadEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val saveSucceededMessage = stringResource(Res.string.setting_download_save_succeeded_message)
    val saveFailedMessage = stringResource(Res.string.setting_download_save_failed_message)

    CollectEffect(effect) { value ->
        val message =
            when (value) {
                is SettingDownloadEffect.SaveSucceeded -> saveSucceededMessage
                is SettingDownloadEffect.SaveFailed -> saveFailedMessage
            }

        coroutineScope.launch { state.hostState.showImmediate(message = message) }
    }
}
