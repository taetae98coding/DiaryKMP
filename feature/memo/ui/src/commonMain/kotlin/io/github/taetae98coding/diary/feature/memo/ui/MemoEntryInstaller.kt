package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class MemoEntryInstaller : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        memoEntry(
            backStack = navigator.backStack,
            homeReselectEvent = navigator.reselectEventOf(MemoHomeNavKey),
        )
    }
}
