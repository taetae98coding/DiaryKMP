package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

internal sealed interface SettingGeminiModelEffect {
    data object InvalidApiKey : SettingGeminiModelEffect

    data object FetchFailed : SettingGeminiModelEffect
}
