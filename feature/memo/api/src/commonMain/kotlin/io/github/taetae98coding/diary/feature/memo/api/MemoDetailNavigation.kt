package io.github.taetae98coding.diary.feature.memo.api

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlin.uuid.Uuid

public fun NavBackStack<ScreenNavKey>.navigateToMemoDetail(id: Uuid) {
    if (lastOrNull() is MemoDetailNavKey) {
        removeLastOrNull()
    }

    add(MemoDetailNavKey(id))
}
