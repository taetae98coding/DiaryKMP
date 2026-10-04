package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.SettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelEffect
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_fetch_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_invalid_api_key_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_save_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_save_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingGeminiScreenEffect(
    state: SettingGeminiFormState,
    effect: Flow<SettingGeminiEffect> = emptyFlow(),
    modelEffect: Flow<SettingGeminiModelEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val saveSucceededMessage = stringResource(Res.string.setting_gemini_save_succeeded_message)
    val saveFailedMessage = stringResource(Res.string.setting_gemini_save_failed_message)
    val invalidApiKeyMessage = stringResource(Res.string.setting_gemini_model_invalid_api_key_message)
    val fetchFailedMessage = stringResource(Res.string.setting_gemini_model_fetch_failed_message)

    CollectEffect(effect) { value ->
        val message =
            when (value) {
                is SettingGeminiEffect.SaveSucceeded -> saveSucceededMessage
                is SettingGeminiEffect.SaveFailed -> saveFailedMessage
            }

        coroutineScope.launch { state.snackbarHostState.showImmediate(message = message) }
    }

    CollectEffect(modelEffect) { value ->
        val message =
            when (value) {
                is SettingGeminiModelEffect.InvalidApiKey -> invalidApiKeyMessage
                is SettingGeminiModelEffect.FetchFailed -> fetchFailedMessage
            }

        coroutineScope.launch { state.snackbarHostState.showImmediate(message = message) }
    }
}
