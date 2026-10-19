package io.github.taetae98coding.diary.feature.setting.ui.file

import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_chosen_directory_supporting
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_default_location_description_android
import org.jetbrains.compose.resources.StringResource

internal actual val settingFileDefaultLocationDescription: StringResource = Res.string.setting_file_default_location_description_android

internal actual fun FileDownloadDirectory.supportingText(): SettingFileLocationSupportingText = SettingFileLocationSupportingText.Resource(resource = Res.string.setting_file_chosen_directory_supporting)
