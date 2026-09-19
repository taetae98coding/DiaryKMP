package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddScreen
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScreen
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<NavKey>.playlistEntry(backStack: NavBackStack<NavKey>) {
    playlistHomeEntry(backStack = backStack)
    musicAddEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.playlistHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<PlaylistHomeNavKey> {
        PlaylistHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { backStack.add(MusicAddNavKey) },
            musicViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<NavKey>.musicAddEntry(backStack: NavBackStack<NavKey>) {
    entry<MusicAddNavKey> {
        MusicAddScreen(
            navigateUp = backStack::removeLastOrNull,
            viewModel = koinViewModel(),
        )
    }
}
