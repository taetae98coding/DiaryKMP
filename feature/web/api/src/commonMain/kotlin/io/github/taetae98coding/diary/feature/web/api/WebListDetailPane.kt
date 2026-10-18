package io.github.taetae98coding.diary.feature.web.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isWebListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = WebHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isWebAddListDetailPaneKey,
    )

// 초기 태그가 있는 WebAdd는 TagDetail 웹 탭에서 진입한 것이다.
private fun ScreenNavKey.isWebAddListDetailPaneKey(): Boolean = this is WebAddNavKey && initialTagId == null
