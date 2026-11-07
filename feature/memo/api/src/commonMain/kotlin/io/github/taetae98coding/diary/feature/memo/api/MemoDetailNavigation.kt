package io.github.taetae98coding.diary.feature.memo.api

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import kotlin.uuid.Uuid

public fun NavBackStack<ScreenNavKey>.navigateToMemoDetail(id: Uuid) {
    navigateToDetail(
        detailKey = MemoDetailNavKey(id),
        isDetailPaneKey = { key -> key is MemoDetailNavKey || key is MemoAddNavKey },
    )
}
