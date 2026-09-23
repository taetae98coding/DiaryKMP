package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

internal sealed interface SettingGeminiModelDialogEvent {
    data object ClickReload : SettingGeminiModelDialogEvent

    data class SelectModel(
        val id: String,
    ) : SettingGeminiModelDialogEvent
}
