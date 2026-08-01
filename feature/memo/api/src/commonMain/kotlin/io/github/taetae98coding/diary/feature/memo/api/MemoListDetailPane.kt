package io.github.taetae98coding.diary.feature.memo.api

import androidx.navigation3.runtime.NavKey

public fun List<NavKey>.isMemoListDetailPane(key: NavKey): Boolean = findMemoDetailPaneListKey(key) { belowKey -> belowKey == MemoHomeNavKey } != null

public fun List<NavKey>.findMemoDetailPaneListKey(
    key: NavKey,
    isListKey: (NavKey) -> Boolean,
): NavKey? {
    val index = lastIndexOf(key)
    if (index < 0 || !key.isMemoDetailPaneKey()) return null

    return subList(0, index)
        .lastOrNull { belowKey -> !belowKey.isMemoDetailPaneKey() }
        ?.takeIf(isListKey)
}

private fun NavKey.isMemoDetailPaneKey(): Boolean = this is MemoAddNavKey || this is MemoDetailNavKey
