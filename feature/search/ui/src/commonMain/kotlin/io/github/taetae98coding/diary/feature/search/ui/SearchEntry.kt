package io.github.taetae98coding.diary.feature.search.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeScreen
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey

internal fun EntryProviderScope<ScreenNavKey>.searchEntry(backStack: NavBackStack<ScreenNavKey>) {
    searchHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.searchHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SearchHomeNavKey> { key ->
        SearchHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToMemoDetail = dropUnlessResumedWithArg { id -> backStack.add(MemoDetailNavKey(id = id)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id = id)) },
            navigateToPlaceDetail = dropUnlessResumedWithArg { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            navigateToWebDetail = dropUnlessResumedWithArg { id -> backStack.add(WebDetailNavKey(id = id)) },
            navigateToDDayDetail = dropUnlessResumedWithArg { id -> backStack.add(DDayDetailNavKey(id = id)) },
            navigateToRoutineDetail = dropUnlessResumedWithArg { id -> backStack.add(RoutineDetailNavKey(id = id, isStandalone = true)) },
            initialType = key.initialType,
            memoViewModel = metroViewModel(),
            tagViewModel = metroViewModel(),
            placeViewModel = metroViewModel(),
            webViewModel = metroViewModel(),
            ddayViewModel = metroViewModel(),
            routineViewModel = metroViewModel(),
        )
    }
}
