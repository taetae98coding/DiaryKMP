package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import kotlin.uuid.Uuid

// 상세 영역에 곡 상세가 놓여 있으면 앞 곡의 상세를 쌓지 않고 새로 고른 곡의 상세로 교체한다.
internal fun NavBackStack<ScreenNavKey>.navigateToMusicDetail(id: Uuid) {
    while (lastOrNull() is MusicDetailNavKey) {
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
