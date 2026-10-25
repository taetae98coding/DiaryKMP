package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker = remember { WasmDirectoryPicker }

private object WasmDirectoryPicker : DirectoryPicker {
    override fun open(): Unit = Unit
}
