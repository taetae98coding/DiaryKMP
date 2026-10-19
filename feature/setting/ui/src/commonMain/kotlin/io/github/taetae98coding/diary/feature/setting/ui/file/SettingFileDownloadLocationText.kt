package io.github.taetae98coding.diary.feature.setting.ui.file

import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import org.jetbrains.compose.resources.StringResource

internal expect val settingFileDefaultLocationDescription: StringResource

internal expect fun FileDownloadDirectory.supportingText(): SettingFileLocationSupportingText
