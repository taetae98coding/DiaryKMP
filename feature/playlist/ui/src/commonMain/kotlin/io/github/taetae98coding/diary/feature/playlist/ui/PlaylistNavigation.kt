package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToMusicDetail(id: Uuid) {
    navigateToDetail(
        detailKey = MusicDetailNavKey(id = id),
        isDetailPaneKey = { key -> key is MusicDetailNavKey || key == MusicAddNavKey },
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromPlaylistHome() {
    navigateUpTo(homeKey = PlaylistHomeNavKey)
}
