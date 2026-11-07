package io.github.taetae98coding.diary.core.navigation

import androidx.navigation3.runtime.EntryProviderScope

public fun interface ScreenEntryInstaller {
    public fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator)
}
