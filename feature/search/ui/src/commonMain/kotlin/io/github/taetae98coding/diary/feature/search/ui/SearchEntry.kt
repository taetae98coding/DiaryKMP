package io.github.taetae98coding.diary.feature.search.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeScreen
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey

public fun EntryProviderScope<ScreenNavKey>.searchEntry(backStack: NavBackStack<ScreenNavKey>) {
    searchHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.searchHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SearchHomeNavKey> { key ->
        SearchHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id = id)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            navigateToPlaceDetail = { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            navigateToWebDetail = { id -> backStack.add(WebDetailNavKey(id = id)) },
            initialType = key.initialType,
        )
    }
}
