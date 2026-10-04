package io.github.taetae98coding.diary.core.navigation

public fun List<ScreenNavKey>.isListDetailPane(
    key: ScreenNavKey,
    homeKey: ScreenNavKey,
    isDetailPaneKey: (ScreenNavKey) -> Boolean,
): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || !isDetailPaneKey(key)) return false

    return subList(0, index).lastOrNull { belowKey -> !isDetailPaneKey(belowKey) } == homeKey
}

public fun MutableList<ScreenNavKey>.navigateToDetail(
    detailKey: ScreenNavKey,
    isDetailPaneKey: (ScreenNavKey) -> Boolean,
) {
    while (lastOrNull()?.let(isDetailPaneKey) == true) {
        removeLastOrNull()
    }

    add(detailKey)
}

public fun MutableList<ScreenNavKey>.navigateUpTo(homeKey: ScreenNavKey) {
    val index = indexOfLast { key -> key == homeKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
