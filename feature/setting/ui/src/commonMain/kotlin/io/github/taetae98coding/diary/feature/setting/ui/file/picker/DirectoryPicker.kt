package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
internal interface DirectoryPicker {
    fun open()
}

internal interface DirectoryPickerFactory {
    @Composable
    fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker
}
