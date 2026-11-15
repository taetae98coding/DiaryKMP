package io.github.taetae98coding.diary.feature.place.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class PlaceEntryInstaller(
    private val contentSharerFactory: ContentSharerFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        placeEntry(backStack = navigator.backStack, contentSharerFactory = contentSharerFactory)
    }
}
