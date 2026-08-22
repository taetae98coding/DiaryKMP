package io.github.taetae98coding.diary.feature.setting.ui.gemini

import io.github.taetae98coding.diary.core.model.gemini.GeminiModel

internal data class SettingGeminiModelUiState(
    val isInProgress: Boolean = false,
    val isLoaded: Boolean = false,
    val modelList: List<GeminiModel> = emptyList(),
    val failure: SettingGeminiModelFailure? = null,
)

internal enum class SettingGeminiModelFailure {
    INVALID_API_KEY,
    UNKNOWN,
}
