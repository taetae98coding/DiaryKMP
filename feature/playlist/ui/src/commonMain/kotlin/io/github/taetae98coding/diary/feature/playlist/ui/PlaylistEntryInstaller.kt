package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class PlaylistEntryInstaller : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        playlistEntry(backStack = navigator.backStack)
    }
}
