package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

internal sealed interface SettingGeminiModelDialogEvent {
    data object ClickRetry : SettingGeminiModelDialogEvent

    data class SelectModel(
        val id: String,
    ) : SettingGeminiModelDialogEvent
}
