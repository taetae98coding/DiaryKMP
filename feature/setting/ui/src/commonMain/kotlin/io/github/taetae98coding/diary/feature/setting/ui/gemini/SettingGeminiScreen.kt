package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.rememberSettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelDialogEvent
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelViewModel
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_api_key_blank_message
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
    val loaded = uiState as? SettingGeminiUiState.Content

    val formState = key(loaded != null) { rememberSettingGeminiFormState(initialSetting = loaded?.setting ?: GeminiSetting.EMPTY) }
    val coroutineScope = rememberCoroutineScope()
    val apiKeyBlankMessage = stringResource(Res.string.setting_gemini_api_key_blank_message)

    SettingGeminiScreenEffect(
        state = formState,
        effect = settingViewModel.effect,
        modelEffect = modelViewModel.effect,
    )

    SettingGeminiScaffold(
        onEvent = { event ->
            when (event) {
                is SettingGeminiScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is SettingGeminiScaffoldEvent.ClickModel -> {
                    when {
                        modelUiState.isLoaded -> {
                            formState.modelDialogState.show()
                        }

                        formState.apiKey.isBlank() -> {
                            coroutineScope.launch { formState.snackbarHostState.showImmediate(message = apiKeyBlankMessage) }
                        }

                        else -> {
                            modelViewModel.fetch(apiKey = formState.apiKey)
                            formState.modelDialogState.show()
                        }
                    }
                }

                is SettingGeminiScaffoldEvent.ClickSave -> {
                    settingViewModel.save(setting = formState.setting)
                }
            }
        },
        onModelDialogEvent = { event ->
            when (event) {
                is SettingGeminiModelDialogEvent.ClickReload -> {
                    if (formState.apiKey.isBlank()) {
                        coroutineScope.launch { formState.snackbarHostState.showImmediate(message = apiKeyBlankMessage) }
                    } else {
                        modelViewModel.fetch(apiKey = formState.apiKey)
                    }
                }

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
