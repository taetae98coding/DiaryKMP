@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListDetailPlaceholder
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListScreen
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.place.ui.add.PlaceAddScreen
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreen
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScreen
import io.github.taetae98coding.diary.feature.place.ui.memo.finished.PlaceMemoFinishedListViewModel
import io.github.taetae98coding.diary.feature.place.ui.place_memo_finished_list_empty_title
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import org.jetbrains.compose.resources.stringResource

internal fun EntryProviderScope<ScreenNavKey>.placeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    contentSharerFactory: ContentSharerFactory,
) {
    placeHomeEntry(backStack = backStack)
    placeAddEntry(backStack = backStack)
    placeDetailEntry(backStack = backStack, contentSharerFactory = contentSharerFactory)
    placeMemoFinishedListEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.placeHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaceHomeNavKey> {
        PlaceHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd =
                dropUnlessResumedWithArg { coordinate ->
                    backStack.add(
                        PlaceAddNavKey(
                            latitude = coordinate?.latitude,
                            longitude = coordinate?.longitude,
                        ),
                    )
                },
            navigateToDetail = dropUnlessResumedWithArg { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            navigateToSearch = dropUnlessResumed { backStack.add(SearchHomeNavKey(initialType = SearchHomeType.PLACE)) },
            mapViewModel = metroViewModel(),
            placeListViewModel = metroViewModel(),
            syncViewModel = metroViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.placeDetailEntry(
    backStack: NavBackStack<ScreenNavKey>,
    contentSharerFactory: ContentSharerFactory,
) {
    entry<PlaceDetailNavKey> { key ->
        val tagAddRequestKey = rememberResultRequestKey()

        PlaceDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id = id)) },
            navigateToMemoAdd = dropUnlessResumed { backStack.add(MemoAddNavKey(initialPlaceId = key.id)) },
            navigateToMemoDetail = dropUnlessResumedWithArg { id -> backStack.add(MemoDetailNavKey(id = id)) },
            navigateToMemoFinishedList = dropUnlessResumed { backStack.add(PlaceMemoFinishedListNavKey(placeId = key.id)) },
            id = key.id,
            tagAddRequestKey = tagAddRequestKey,
            detailViewModel = assistedMetroViewModel<PlaceDetailViewModel, PlaceDetailViewModel.Factory> { create(id = key.id) },
            searchViewModel = metroViewModel(),
            tagViewModel = assistedMetroViewModel<EntityDetailTagViewModel, EntityDetailTagViewModel.Factory> { create(kind = EntityTagKind.PLACE, entityId = key.id) },
            contentSharerFactory = contentSharerFactory,
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.placeAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaceAddNavKey> { key ->
        val tagAddRequestKey = rememberResultRequestKey()

        PlaceAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            addedResultRequestKey = key.requestKey,
            initialCoordinate = key.toCoordinateOrNull(),
            addViewModel = metroViewModel(),
            searchViewModel = metroViewModel(),
            tagViewModel = assistedMetroViewModel<EntityAddTagViewModel, EntityAddTagViewModel.Factory> { create(initialTagId = key.initialTagId) },
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

private fun EntryProviderScope<ScreenNavKey>.placeMemoFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<PlaceMemoFinishedListNavKey>(
        metadata = ::placeMemoFinishedListPaneMetadata,
    ) { key ->
        EntityMemoFinishedListScreen(
            navigateUp = backStack::navigateUpFromPlaceMemoFinishedList,
            navigateToMemoDetail = backStack::navigateToMemoDetail,
            emptyTitle = stringResource(Res.string.place_memo_finished_list_empty_title),
            memoViewModel = assistedMetroViewModel<PlaceMemoFinishedListViewModel, PlaceMemoFinishedListViewModel.Factory> { create(placeId = key.placeId) },
            syncViewModel = metroViewModel(),
        )
    }
}

internal fun placeMemoFinishedListPaneMetadata(key: PlaceMemoFinishedListNavKey): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = key,
        detailPlaceholder = { EntityMemoFinishedListDetailPlaceholder() },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

internal fun NavBackStack<ScreenNavKey>.navigateUpFromPlaceMemoFinishedList() {
    val index = indexOfLast { key -> key is PlaceMemoFinishedListNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
