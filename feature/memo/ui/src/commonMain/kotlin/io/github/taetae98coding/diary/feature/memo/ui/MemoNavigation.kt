package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToMemoDetailFromHome(id: Uuid) {
    if (lastOrNull() is MemoDetailNavKey) {
        removeLastOrNull()
    }

    add(MemoDetailNavKey(id))
}

internal fun NavBackStack<ScreenNavKey>.navigateToCopiedMemo(id: Uuid) {
    removeLastOrNull()
    add(MemoDetailNavKey(id))
}

internal fun NavBackStack<ScreenNavKey>.navigateToTagAddFromMemoHomeFilter() {
    removeLastOrNull()
    add(TagAddNavKey())
}
