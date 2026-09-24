package io.github.taetae98coding.diary.feature.place.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.place.ui.add.PlaceAddScreen
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreen
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScreen
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.placeEntry(backStack: NavBackStack<ScreenNavKey>) {
    placeHomeEntry(backStack = backStack)
    placeAddEntry(backStack = backStack)
    placeDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.placeHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaceHomeNavKey> {
        PlaceHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { coordinate ->
                backStack.add(
                    PlaceAddNavKey(
                        latitude = coordinate?.latitude,
                        longitude = coordinate?.longitude,
                    ),
                )
            },
            navigateToDetail = { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            navigateToSearch = { backStack.add(SearchHomeNavKey(initialType = SearchHomeType.PLACE)) },
            mapViewModel = koinViewModel(),
            placeListViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.placeDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaceDetailNavKey> { key ->
        val tagAddRequestKey = rememberResultRequestKey()

        PlaceDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            navigateToMemoAdd = { backStack.add(MemoAddNavKey(initialPlaceId = key.id)) },
            navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id = id)) },
            id = key.id,
            tagAddRequestKey = tagAddRequestKey,
            detailViewModel = koinViewModel { parametersOf(key.id) },
            searchViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.placeAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaceAddNavKey> { key ->
        val tagAddRequestKey = rememberResultRequestKey()

        PlaceAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            initialCoordinate = key.toCoordinateOrNull(),
            addViewModel = koinViewModel(),
            searchViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.initialTagId) },
        )
    }
}

private fun PlaceAddNavKey.toCoordinateOrNull(): Coordinate? {
    val latitude = latitude
    val longitude = longitude

    return if (latitude == null || longitude == null) {
        null
    } else {
        Coordinate(latitude = latitude, longitude = longitude)
    }
}
