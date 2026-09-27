package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToMemoDetailFromHome(id: Uuid) {
    if (lastOrNull() is MemoDetailNavKey) {
        removeLastOrNull()
    }

    add(MemoDetailNavKey(id))
}

internal fun NavBackStack<ScreenNavKey>.navigateToMemoAddFromHome() {
    add(MemoAddNavKey())
}

internal fun NavBackStack<ScreenNavKey>.navigateToSearchFromMemoHome() {
    add(SearchHomeNavKey(initialType = SearchHomeType.MEMO))
}

internal fun NavBackStack<ScreenNavKey>.navigateToCopiedMemo(id: Uuid) {
    removeLastOrNull()
    add(MemoDetailNavKey(id))
}

internal fun NavBackStack<ScreenNavKey>.navigateToTagAddFromMemoHomeFilter() {
    removeLastOrNull()
    add(TagAddNavKey())
}

internal fun NavBackStack<ScreenNavKey>.navigateToWebAddFromMemoWebInput() {
    add(WebAddNavKey())
}
