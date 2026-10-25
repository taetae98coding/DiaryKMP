package io.github.taetae98coding.diary.feature.setting.ui.file

import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_chosen_directory_supporting
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_default_location_label
import org.jetbrains.compose.resources.StringResource

// 공통 화면이 컴파일되도록 자리만 채운다.
internal actual val settingFileDefaultLocationDescription: StringResource = Res.string.setting_file_default_location_label

internal actual fun FileDownloadDirectory.supportingText(): SettingFileLocationSupportingText = SettingFileLocationSupportingText.Resource(resource = Res.string.setting_file_chosen_directory_supporting)
