package io.github.taetae98coding.diary.feature.setting.ui.browser

internal sealed interface SettingBrowserScaffoldEvent {
    data object ClickNavigateUp : SettingBrowserScaffoldEvent

    data class SelectProfile(
        val directory: String,
    ) : SettingBrowserScaffoldEvent
}
