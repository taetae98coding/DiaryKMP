@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.isPlaylistAddOnDetailPane
import io.github.taetae98coding.diary.feature.playlist.api.isPlaylistListDetailPane
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddScreen
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailScreen
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeScreen
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey

internal fun EntryProviderScope<ScreenNavKey>.playlistEntry(
    backStack: NavBackStack<ScreenNavKey>,
    contentSharerFactory: ContentSharerFactory,
) {
    playlistHomeEntry(backStack = backStack)
    musicAddEntry(backStack = backStack)
    musicDetailEntry(backStack = backStack, contentSharerFactory = contentSharerFactory)
}

private fun EntryProviderScope<ScreenNavKey>.playlistHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaylistHomeNavKey>(
        clazzContentKey = { PLAYLIST_HOME_CONTENT_KEY },
        metadata = playlistHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        PlaylistHomeScreen(
            navigateUp = backStack::navigateUpFromPlaylistHome,
            navigateToAdd = dropUnlessResumed(block = backStack::navigateToMusicAddFromHome),
            navigateToDetail = { id -> backStack.navigateToMusicDetail(id = id) },
            componentVisibleProvider = { PlaylistHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isPlaylistAddOnDetailPane()) },
            musicViewModel = metroViewModel(),
            syncViewModel = metroViewModel(),
        )
    }
}

internal const val PLAYLIST_HOME_CONTENT_KEY: String = "PlaylistHomeNavKey"

internal fun playlistHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = PlaylistHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = PLAYLIST_HOME_CONTENT_KEY) {
                val tagAddRequestKey = rememberResultRequestKey()

                MusicAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
                    navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id = id)) },
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    addViewModel = metroViewModel(),
                    tagViewModel = assistedMetroViewModel<EntityAddTagViewModel, EntityAddTagViewModel.Factory> { create(initialTagId = null) },
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.musicAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MusicAddNavKey>(
        metadata = { key -> backStack.playlistListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        MusicAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { MusicAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            addViewModel = metroViewModel(),
            tagViewModel = assistedMetroViewModel<EntityAddTagViewModel, EntityAddTagViewModel.Factory> { create(initialTagId = key.initialTagId) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.musicDetailEntry(
    backStack: NavBackStack<ScreenNavKey>,
    contentSharerFactory: ContentSharerFactory,
) {
    entry<MusicDetailNavKey>(
        metadata = { key -> backStack.playlistListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        MusicDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { MusicDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            musicViewModel = assistedMetroViewModel<MusicDetailViewModel, MusicDetailViewModel.Factory> { create(id = key.id) },
            tagViewModel = assistedMetroViewModel<EntityDetailTagViewModel, EntityDetailTagViewModel.Factory> { create(kind = EntityTagKind.MUSIC, entityId = key.id) },
            contentSharerFactory = contentSharerFactory,
        )
    }
}

private fun NavBackStack<ScreenNavKey>.playlistListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isPlaylistListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = PlaylistHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
