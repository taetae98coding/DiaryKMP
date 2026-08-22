package io.github.taetae98coding.diary.feature.setting.ui.gemini

internal sealed interface SettingGeminiEffect {
    data object SaveSucceeded : SettingGeminiEffect

    data object SaveFailed : SettingGeminiEffect
}
