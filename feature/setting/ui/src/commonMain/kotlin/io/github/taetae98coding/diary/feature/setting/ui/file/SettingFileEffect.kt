package io.github.taetae98coding.diary.feature.setting.ui.file

internal sealed interface SettingFileEffect {
    data object ChangeFailed : SettingFileEffect
}
