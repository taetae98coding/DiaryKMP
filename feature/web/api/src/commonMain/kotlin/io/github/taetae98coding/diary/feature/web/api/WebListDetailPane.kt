package io.github.taetae98coding.diary.feature.web.api

import androidx.navigation3.runtime.NavKey

public fun List<NavKey>.isWebListDetailPane(key: NavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isWebAddListDetailPaneKey()) return false

    return subList(0, index).lastOrNull { belowKey -> !belowKey.isWebAddListDetailPaneKey() } == WebHomeNavKey
}

// TagDetail 웹 탭에서 초기 태그와 함께 진입한 WebAdd는 단독으로 표시하므로 이 배치에 참여하지 않는다.
private fun NavKey.isWebAddListDetailPaneKey(): Boolean = this is WebAddNavKey && initialTagId == null
