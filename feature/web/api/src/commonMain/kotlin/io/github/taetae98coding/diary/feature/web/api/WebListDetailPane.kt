package io.github.taetae98coding.diary.feature.web.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun List<ScreenNavKey>.isWebListDetailPane(key: ScreenNavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isWebAddListDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isWebAddListDetailPaneKey() } == WebHomeNavKey
}

// 초기 태그가 있는 WebAdd는 TagDetail 웹 탭에서 진입한 것이다.
private fun ScreenNavKey.isWebAddListDetailPaneKey(): Boolean = this is WebAddNavKey && initialTagId == null
