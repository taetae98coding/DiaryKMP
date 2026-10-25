package io.github.taetae98coding.diary.feature.setting.ui.file

import org.jetbrains.compose.resources.StringResource

internal sealed interface SettingFileLocationSupportingText {
    data class Resource(
        val resource: StringResource,
    ) : SettingFileLocationSupportingText

    data class Text(
        val text: String,
    ) : SettingFileLocationSupportingText
}
