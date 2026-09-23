package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey

internal fun NavBackStack<ScreenNavKey>.navigateUpFromPlaylistHome() {
    val index = indexOfLast { key -> key == PlaylistHomeNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
