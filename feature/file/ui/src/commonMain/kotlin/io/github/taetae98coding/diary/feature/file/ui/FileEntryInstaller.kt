package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePickerFactory
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class FileEntryInstaller(
    private val filePickerFactory: FilePickerFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        fileEntry(backStack = navigator.backStack, filePickerFactory = filePickerFactory)
    }
}
