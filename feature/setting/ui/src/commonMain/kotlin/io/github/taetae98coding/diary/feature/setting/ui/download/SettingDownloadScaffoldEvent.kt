package io.github.taetae98coding.diary.feature.setting.ui.download

internal sealed interface SettingDownloadScaffoldEvent {
    data object ClickNavigateUp : SettingDownloadScaffoldEvent

    data object ClickSave : SettingDownloadScaffoldEvent
}
