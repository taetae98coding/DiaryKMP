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

    return key == PlaylistHomeNavKey || (key is MusicAddNavKey && isPlaylistListDetailPane(key))
}

// 초기 태그가 있는 MusicAdd는 TagDetail 곡 탭에서 진입한 것이다.
private fun ScreenNavKey.isPlaylistDetailPaneKey(): Boolean = (this is MusicAddNavKey && initialTagId == null) || (this is MusicDetailNavKey && !isStandalone)
