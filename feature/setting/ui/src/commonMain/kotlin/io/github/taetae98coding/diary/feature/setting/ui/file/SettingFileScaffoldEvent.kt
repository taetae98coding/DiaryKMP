package io.github.taetae98coding.diary.feature.setting.ui.file

internal sealed interface SettingFileScaffoldEvent {
    data object ClickNavigateUp : SettingFileScaffoldEvent

    data object ClickChooseDirectory : SettingFileScaffoldEvent

    data object ClickResetDirectory : SettingFileScaffoldEvent
}
