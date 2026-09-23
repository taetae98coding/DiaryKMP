package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun List<ScreenNavKey>.isPlaylistListDetailPane(key: ScreenNavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isPlaylistDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isPlaylistDetailPaneKey() } == PlaylistHomeNavKey
}

private fun ScreenNavKey.isPlaylistDetailPaneKey(): Boolean = this is MusicAddNavKey || this is MusicDetailNavKey
