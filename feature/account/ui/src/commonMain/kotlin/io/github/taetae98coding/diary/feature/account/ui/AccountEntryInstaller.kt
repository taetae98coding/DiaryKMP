package io.github.taetae98coding.diary.feature.account.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class AccountEntryInstaller : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        accountEntry(backStack = navigator.backStack)
    }
}
