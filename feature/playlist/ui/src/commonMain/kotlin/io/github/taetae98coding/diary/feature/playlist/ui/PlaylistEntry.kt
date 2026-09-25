@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.isPlaylistAddOnDetailPane
import io.github.taetae98coding.diary.feature.playlist.api.isPlaylistListDetailPane
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddScreen
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailScreen
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.playlistEntry(backStack: NavBackStack<ScreenNavKey>) {
    playlistHomeEntry(backStack = backStack)
    musicAddEntry(backStack = backStack)
    musicDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.playlistHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaylistHomeNavKey>(
        clazzContentKey = { PLAYLIST_HOME_CONTENT_KEY },
        metadata = playlistHomeListPaneMetadata(),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        PlaylistHomeScreen(
            navigateUp = backStack::navigateUpFromPlaylistHome,
            navigateToAdd = { backStack.add(MusicAddNavKey) },
            navigateToDetail = { id -> backStack.navigateToMusicDetail(id = id) },
            componentVisibleProvider = { PlaylistHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isPlaylistAddOnDetailPane()) },
            musicViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
            downloadViewModel = koinViewModel(),
        )
    }
}

internal const val PLAYLIST_HOME_CONTENT_KEY: String = "PlaylistHomeNavKey"

internal fun playlistHomeListPaneMetadata(): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = PlaylistHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = PLAYLIST_HOME_CONTENT_KEY) {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    viewModel = koinViewModel(),
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.musicAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MusicAddNavKey>(
        metadata = { key -> backStack.playlistListDetailPaneMetadata(key) },
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        MusicAddScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { MusicAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.musicDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MusicDetailNavKey>(
        metadata = { key -> backStack.playlistListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        MusicDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { MusicDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.playlistListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isPlaylistListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = PlaylistHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
