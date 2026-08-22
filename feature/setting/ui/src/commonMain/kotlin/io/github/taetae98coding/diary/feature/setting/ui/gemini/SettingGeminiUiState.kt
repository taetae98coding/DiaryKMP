package io.github.taetae98coding.diary.feature.setting.ui.gemini

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting

internal sealed interface SettingGeminiUiState {
    data object Loading : SettingGeminiUiState

    data class Loaded(
        val setting: GeminiSetting,
        val isInProgress: Boolean = false,
    ) : SettingGeminiUiState
}
