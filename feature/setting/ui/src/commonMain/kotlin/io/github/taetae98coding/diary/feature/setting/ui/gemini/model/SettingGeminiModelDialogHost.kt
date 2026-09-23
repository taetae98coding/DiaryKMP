package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel

@Composable
internal fun SettingGeminiModelDialogHost(
    dialogState: DialogState,
    onEvent: (SettingGeminiModelDialogEvent) -> Unit,
    selectedModelProvider: () -> String = { "" },
    uiStateProvider: () -> SettingGeminiModelUiState = { SettingGeminiModelUiState() },
) {
    if (!dialogState.isVisible) return

    SettingGeminiModelDialog(
        onDismissRequest = { dialogState.hide() },
        onEvent = { event ->
            // 모델을 고르는 것이 곧 확인이므로 고르면 대화상자를 닫는다.
            if (event is SettingGeminiModelDialogEvent.SelectModel) dialogState.hide()
            onEvent(event)
        },
        selectedModelProvider = selectedModelProvider,
        uiStateProvider = uiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun SettingGeminiModelDialogHostPreview() {
    DiaryTheme {
        SettingGeminiModelDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            selectedModelProvider = { "models/gemini-3.6-flash" },
            uiStateProvider = {
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList =
                        listOf(
                            GeminiModel(id = "models/gemini-3.6-flash", displayName = "Gemini 3.6 Flash", description = "빠른 범용 모델"),
                            GeminiModel(id = "models/gemini-3.6-pro", displayName = "Gemini 3.6 Pro", description = ""),
                        ),
                )
            },
        )
    }
}
