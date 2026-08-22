package io.github.taetae98coding.diary.feature.setting.ui.gemini

internal sealed interface SettingGeminiScaffoldEvent {
    data object ClickNavigateUp : SettingGeminiScaffoldEvent

    data object ClickModel : SettingGeminiScaffoldEvent

    data object ClickSave : SettingGeminiScaffoldEvent
}
