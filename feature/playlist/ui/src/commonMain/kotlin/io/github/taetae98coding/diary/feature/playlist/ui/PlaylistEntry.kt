package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddScreen
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScreen
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.playlistEntry(backStack: NavBackStack<ScreenNavKey>) {
    playlistHomeEntry(backStack = backStack)
    musicAddEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.playlistHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaylistHomeNavKey> {
        PlaylistHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { backStack.add(MusicAddNavKey) },
            musicViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.musicAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MusicAddNavKey> {
        MusicAddScreen(
            navigateUp = backStack::removeLastOrNull,
            viewModel = koinViewModel(),
        )
    }
}
