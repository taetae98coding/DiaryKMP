package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isPlaylistListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = PlaylistHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isPlaylistDetailPaneKey,
    )

public fun List<ScreenNavKey>.isPlaylistAddOnDetailPane(): Boolean {
    val key = lastOrNull() ?: return false

    return key == PlaylistHomeNavKey || (key == MusicAddNavKey && isPlaylistListDetailPane(key))
}

private fun ScreenNavKey.isPlaylistDetailPaneKey(): Boolean = this is MusicAddNavKey || this is MusicDetailNavKey
