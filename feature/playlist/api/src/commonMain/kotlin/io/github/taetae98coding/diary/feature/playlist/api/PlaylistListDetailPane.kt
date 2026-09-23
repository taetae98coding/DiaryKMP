package io.github.taetae98coding.diary.feature.playlist.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun List<ScreenNavKey>.isPlaylistListDetailPane(key: ScreenNavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || key != MusicAddNavKey) return false

    return subList(0, index).lastOrNull { belowKey -> belowKey != MusicAddNavKey } == PlaylistHomeNavKey
}
