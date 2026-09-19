package io.github.taetae98coding.diary.feature.memo.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey

public fun List<ScreenNavKey>.isMemoListDetailPane(key: ScreenNavKey): Boolean = findMemoDetailPaneListKey(key) { belowKey -> belowKey == MemoHomeNavKey } != null

public fun List<ScreenNavKey>.findMemoDetailPaneListKey(
    key: ScreenNavKey,
    isListKey: (ScreenNavKey) -> Boolean,
): ScreenNavKey? {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isMemoDetailPaneKey()) return null

    return subList(0, index)
        .lastOrNull { belowKey -> !belowKey.isMemoDetailPaneKey() }
        ?.takeIf(isListKey)
}

private fun ScreenNavKey.isMemoDetailPaneKey(): Boolean = this is MemoAddNavKey || this is MemoDetailNavKey
