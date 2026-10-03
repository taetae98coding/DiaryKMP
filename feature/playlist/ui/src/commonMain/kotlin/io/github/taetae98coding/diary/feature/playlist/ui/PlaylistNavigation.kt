package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToMusicDetail(id: Uuid) {
    while (lastOrNull().let { key -> key is MusicDetailNavKey || key == MusicAddNavKey }) {
        removeLastOrNull()
    }

    add(MusicDetailNavKey(id = id))
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromPlaylistHome() {
    val index = indexOfLast { key -> key == PlaylistHomeNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
