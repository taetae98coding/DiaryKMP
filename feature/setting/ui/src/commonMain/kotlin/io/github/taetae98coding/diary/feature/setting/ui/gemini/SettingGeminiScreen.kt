package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.SettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.rememberSettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelDialogEvent
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelFailure
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelUiState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelViewModel
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_api_key_blank_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_fetch_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_invalid_api_key_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_save_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_save_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingGeminiScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingGeminiScaffoldComponentVisible,
    settingViewModel: SettingGeminiViewModel,
    modelViewModel: SettingGeminiModelViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by settingViewModel.uiState.collectAsStateWithLifecycle()
    val modelUiState by modelViewModel.uiState.collectAsStateWithLifecycle()
    val loaded = uiState as? SettingGeminiUiState.Loaded

    val formState = key(loaded != null) { rememberSettingGeminiFormState(initialSetting = loaded?.setting ?: GeminiSetting.EMPTY) }
    val coroutineScope = rememberCoroutineScope()
    val apiKeyBlankMessage = stringResource(Res.string.setting_gemini_api_key_blank_message)

    FetchModelEffect(
        state = formState,
        modelViewModel = modelViewModel,
        modelUiStateProvider = { modelUiState },
    )
    SaveEffect(
        state = formState,
        effect = settingViewModel.effect,
    )
    ModelFailureEffect(
        state = formState,
        effect = modelViewModel.effect,
    )

    SettingGeminiScaffold(
        onEvent = { event ->
            when (event) {
                is SettingGeminiScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is SettingGeminiScaffoldEvent.ClickModel -> {
                    if (formState.apiKey.isBlank()) {
                        coroutineScope.launch { formState.hostState.showImmediate(message = apiKeyBlankMessage) }
                    } else {
                        formState.modelDialogState.show()
                    }
                }

                is SettingGeminiScaffoldEvent.ClickSave -> {
                    settingViewModel.save(setting = formState.setting)
                }
            }
        },
        onModelDialogEvent = { event ->
            when (event) {
                is SettingGeminiModelDialogEvent.ClickReload -> modelViewModel.fetch(apiKey = formState.apiKey)
                is SettingGeminiModelDialogEvent.SelectModel -> formState.model = event.id
            }
        },
        modifier = modifier,
        state = formState,
        uiStateProvider = { uiState },
        modelUiStateProvider = { modelUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}

@Composable
private fun FetchModelEffect(
    state: SettingGeminiFormState,
    modelViewModel: SettingGeminiModelViewModel,
    modelUiStateProvider: () -> SettingGeminiModelUiState,
) {
    LaunchedEffect(state, modelViewModel) {
        snapshotFlow { state.modelDialogState.isVisible }
            .filter { isDialogVisible -> isDialogVisible }
            .collect {
                if (modelUiStateProvider().isLoaded) return@collect

                modelViewModel.fetch(apiKey = state.apiKey)
            }
    }
}

@Composable
private fun SaveEffect(
    state: SettingGeminiFormState,
    effect: Flow<SettingGeminiEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val saveSucceededMessage = stringResource(Res.string.setting_gemini_save_succeeded_message)
    val saveFailedMessage = stringResource(Res.string.setting_gemini_save_failed_message)

    CollectEffect(effect) { value ->
        val message =
            when (value) {
                is SettingGeminiEffect.SaveSucceeded -> saveSucceededMessage
                is SettingGeminiEffect.SaveFailed -> saveFailedMessage
            }

        coroutineScope.launch { state.hostState.showImmediate(message = message) }
    }
}

@Composable
private fun ModelFailureEffect(
    state: SettingGeminiFormState,
    effect: Flow<SettingGeminiModelFailure> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val invalidApiKeyMessage = stringResource(Res.string.setting_gemini_model_invalid_api_key_message)
    val fetchFailedMessage = stringResource(Res.string.setting_gemini_model_fetch_failed_message)

    CollectEffect(effect) { failure ->
        val message =
            when (failure) {
                SettingGeminiModelFailure.INVALID_API_KEY -> invalidApiKeyMessage
                SettingGeminiModelFailure.UNKNOWN -> fetchFailedMessage
            }

        coroutineScope.launch { state.hostState.showImmediate(message = message) }
    }
}
