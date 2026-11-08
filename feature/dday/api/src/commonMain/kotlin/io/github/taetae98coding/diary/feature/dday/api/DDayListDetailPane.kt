package io.github.taetae98coding.diary.feature.dday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isDDayListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = DDayHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isDDayDetailPaneKey,
    )

public fun List<ScreenNavKey>.isDDayAddOnDetailPane(): Boolean {
    // 필터는 목록 위에 겹쳐 열리므로 상세 영역에 놓인 화면을 정하지 않는다.
    val key = lastOrNull { key -> key != DDayHomeFilterNavKey } ?: return false

    return key == DDayHomeNavKey || (key is DDayAddNavKey && isDDayListDetailPane(key))
}

// 초기 태그가 있는 DDayAdd는 TagDetail 디데이 탭에서 진입한 것이다.
private fun ScreenNavKey.isDDayDetailPaneKey(): Boolean = (this is DDayAddNavKey && initialTagId == null) || this is DDayDetailNavKey
