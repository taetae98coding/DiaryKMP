package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<DirectoryPickerFactory>())
internal class WasmDirectoryPickerFactory : DirectoryPickerFactory {
    @Composable
    override fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker = remember { WasmDirectoryPicker }
}

private object WasmDirectoryPicker : DirectoryPicker {
    override fun open(): Unit = Unit
}
