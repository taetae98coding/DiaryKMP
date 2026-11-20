package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isRoutineListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = RoutineHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isRoutineDetailPaneKey,
    )

public fun List<ScreenNavKey>.findRoutineDetailPaneListKey(
    key: ScreenNavKey,
    isListKey: (ScreenNavKey) -> Boolean,
): ScreenNavKey? {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isRoutineDetailPaneKey()) return null

    return subList(0, index)
        .lastOrNull { belowKey -> !belowKey.isRoutineDetailPaneKey() }
        ?.takeIf(isListKey)
}

public fun List<ScreenNavKey>.isRoutineAddOnDetailPane(): Boolean {
    // 필터는 목록 위에 겹쳐 열리므로 상세 영역에 놓인 화면을 정하지 않는다.
    val key = lastOrNull { key -> key != RoutineHomeFilterNavKey } ?: return false

    return key == RoutineHomeNavKey || (key is RoutineAddNavKey && isRoutineListDetailPane(key))
}

private fun ScreenNavKey.isRoutineDetailPaneKey(): Boolean = topLevelListKey == RoutineHomeNavKey
