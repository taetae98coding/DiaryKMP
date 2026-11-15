package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.annotation.Single

@Single(binds = [DirectoryPickerFactory::class])
internal class WasmDirectoryPickerFactory : DirectoryPickerFactory {
    @Composable
    override fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker = remember { WasmDirectoryPicker }
}

private object WasmDirectoryPicker : DirectoryPicker {
    override fun open(): Unit = Unit
}
