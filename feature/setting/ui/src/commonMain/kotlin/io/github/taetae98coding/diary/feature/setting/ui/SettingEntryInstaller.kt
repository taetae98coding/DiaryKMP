package io.github.taetae98coding.diary.feature.setting.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.setting.ui.file.picker.DirectoryPickerFactory
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class SettingEntryInstaller(
    private val directoryPickerFactory: DirectoryPickerFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        settingEntry(backStack = navigator.backStack, directoryPickerFactory = directoryPickerFactory)
    }
}
