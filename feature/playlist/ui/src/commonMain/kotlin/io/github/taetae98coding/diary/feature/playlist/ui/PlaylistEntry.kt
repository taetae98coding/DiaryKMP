package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScreen

public fun EntryProviderScope<NavKey>.playlistEntry(backStack: NavBackStack<NavKey>) {
    playlistHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.playlistHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<PlaylistHomeNavKey> {
        PlaylistHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
