package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class TagEntryInstaller : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        tagEntry(
            backStack = navigator.backStack,
            homeReselectEvent = navigator.reselectEventOf(TagHomeNavKey),
        )
    }
}
