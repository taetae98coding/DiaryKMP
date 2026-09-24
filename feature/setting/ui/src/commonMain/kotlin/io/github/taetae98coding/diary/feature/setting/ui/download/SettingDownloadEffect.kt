package io.github.taetae98coding.diary.feature.setting.ui.download

internal sealed interface SettingDownloadEffect {
    data object SaveSucceeded : SettingDownloadEffect

    data object SaveFailed : SettingDownloadEffect
}
