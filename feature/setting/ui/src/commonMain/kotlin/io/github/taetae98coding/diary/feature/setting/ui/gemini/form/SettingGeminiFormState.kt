package io.github.taetae98coding.diary.feature.setting.ui.gemini.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting

@Stable
internal class SettingGeminiFormState(
    val apiKeyState: TextFieldState,
    val systemPromptState: TextFieldState,
    val systemPromptFocusRequester: FocusRequester,
    val hostState: SnackbarHostState,
    val modelDialogState: DialogState,
    initialModel: String,
) {
    var model: String by mutableStateOf(initialModel)

    var isApiKeyVisible: Boolean by mutableStateOf(false)

    val apiKey: String
        get() = apiKeyState.text.toString()

    val setting: GeminiSetting
        get() =
            GeminiSetting(
                apiKey = apiKey,
                model = model,
                systemPrompt = systemPromptState.text.toString(),
            )

    companion object {
        fun saver(
            apiKeyState: TextFieldState,
            systemPromptState: TextFieldState,
            systemPromptFocusRequester: FocusRequester,
            hostState: SnackbarHostState,
            modelDialogState: DialogState,
        ): Saver<SettingGeminiFormState, String> =
            Saver(
                save = { state -> state.model },
                restore = { model ->
                    SettingGeminiFormState(
                        apiKeyState = apiKeyState,
                        systemPromptState = systemPromptState,
                        systemPromptFocusRequester = systemPromptFocusRequester,
                        hostState = hostState,
                        modelDialogState = modelDialogState,
                        initialModel = model,
                    )
                },
            )
    }
}

@Composable
internal fun rememberSettingGeminiFormState(initialSetting: GeminiSetting = GeminiSetting.EMPTY): SettingGeminiFormState {
    val apiKeyState = rememberTextFieldState(initialText = initialSetting.apiKey)
    val systemPromptState = rememberTextFieldState(initialText = initialSetting.systemPrompt)
    val systemPromptFocusRequester = remember { FocusRequester() }
    val hostState = remember { SnackbarHostState() }
    val modelDialogState = remember { DialogState() }

    return rememberSaveable(
        apiKeyState,
        systemPromptState,
        systemPromptFocusRequester,
        hostState,
        modelDialogState,
        saver =
            SettingGeminiFormState.saver(
                apiKeyState = apiKeyState,
                systemPromptState = systemPromptState,
                systemPromptFocusRequester = systemPromptFocusRequester,
                hostState = hostState,
                modelDialogState = modelDialogState,
            ),
    ) {
        SettingGeminiFormState(
            apiKeyState = apiKeyState,
            systemPromptState = systemPromptState,
            systemPromptFocusRequester = systemPromptFocusRequester,
            hostState = hostState,
            modelDialogState = modelDialogState,
            initialModel = initialSetting.model,
        )
    }
}
