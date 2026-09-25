@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.isTagListDetailPane
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.finished.TagFinishedListScreen
import io.github.taetae98coding.diary.feature.tag.ui.home.ScrollToFirstTagOnReselectEffect
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeScreen
import io.github.taetae98coding.diary.feature.tag.ui.home.filter.TagHomeFilterContent
import io.github.taetae98coding.diary.feature.tag.ui.memo.finished.TagMemoFinishedListDetailPlaceholder
import io.github.taetae98coding.diary.feature.tag.ui.memo.finished.TagMemoFinishedListScreen
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.tagEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    tagHomeEntry(
        backStack = backStack,
        homeReselectEvent = homeReselectEvent,
    )
    tagHomeFilterEntry()
    tagFinishedListEntry(backStack = backStack)
    tagAddEntry(backStack = backStack)
    tagDetailEntry(backStack = backStack)
    tagMemoFinishedListEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.tagHomeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    entry<TagHomeNavKey>(
        clazzContentKey = { TAG_HOME_CONTENT_KEY },
        metadata = tagHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)
        val gridState = rememberLazyGridState()

        ScrollToFirstTagOnReselectEffect(
            reselectEvent = homeReselectEvent,
            gridState = gridState,
        )
        TagHomeScreen(
            navigateToAdd = { backStack.navigateToTagAdd() },
            navigateToDetail = { id -> backStack.navigateToTagDetail(id) },
            navigateToFilter = {
                backStack.add(TagHomeFilterNavKey)
            },
            navigateToFinishedList = {
                backStack.add(TagFinishedListNavKey)
            },
            navigateToSearch = {
                backStack.add(SearchHomeNavKey(initialType = SearchHomeType.TAG))
            },
            componentVisibleProvider = {
                val isTagDetailVisible = backStack.lastOrNull() is TagDetailNavKey
                val isAddPaneVisible = isDetailPaneVisible && !isTagDetailVisible

                TagHomeScaffoldComponentVisible(isAddButtonVisible = !isAddPaneVisible)
            },
            gridState = gridState,
            tagViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal const val TAG_HOME_CONTENT_KEY: String = "TagHomeNavKey"

internal fun tagHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = TagHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = TAG_HOME_CONTENT_KEY) {
                TagAddDetailPlaceholder(backStack = backStack)
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

@Composable
private fun TagAddDetailPlaceholder(backStack: NavBackStack<ScreenNavKey>) {
    val tagAddRequestKey = rememberResultRequestKey()

    TagAddScreen(
        navigateUp = {},
        navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
        navigateToDetail = { id -> backStack.navigateToTagDetail(id) },
        addedResultRequestKey = null,
        tagAddRequestKey = tagAddRequestKey,
        componentVisibleProvider = { TagAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
        addViewModel = koinViewModel(),
        linkViewModel = koinViewModel(),
    )
}

private fun EntryProviderScope<ScreenNavKey>.tagHomeFilterEntry() {
    entry<TagHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        TagHomeFilterContent()
    }
}

private fun EntryProviderScope<ScreenNavKey>.tagFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<TagFinishedListNavKey> {
        TagFinishedListScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToDetail = { id -> backStack.add(TagDetailNavKey(id)) },
            tagViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.tagAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<TagAddNavKey>(
        metadata = { key -> backStack.tagListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        TagAddScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToDetail = { id -> backStack.add(TagDetailNavKey(id)) },
            addedResultRequestKey = key.requestKey,
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { TagAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            addViewModel = koinViewModel(),
            linkViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.tagDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<TagDetailNavKey>(
        clazzContentKey = { key -> backStack.tagDetailContentKey(key) },
        metadata = { key -> backStack.tagListDetailPaneMetadata(key) },
    ) { navKey ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = key(navKey.id) { rememberResultRequestKey() }

        TagDetailScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToDetail = { id -> backStack.add(TagDetailNavKey(id)) },
            navigateToMemoAdd = { backStack.add(MemoAddNavKey(primaryTagId = navKey.id)) },
            navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id = id)) },
            navigateToMemoFinishedList = { backStack.add(TagMemoFinishedListNavKey(tagId = navKey.id)) },
            navigateToWebAdd = { backStack.navigateToWebAddFromTagDetail(tagId = navKey.id) },
            navigateToWebDetail = { id -> backStack.add(WebDetailNavKey(id = id)) },
            navigateToPlaceAdd = { coordinate -> backStack.navigateToPlaceAddFromTagDetail(tagId = navKey.id, coordinate = coordinate) },
            navigateToPlaceDetail = { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            id = navKey.id,
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = {
                TagDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible || backStack.isNavigatedFromTagDetail(navKey))
            },
            // 목록에서 다른 태그를 고르면 같은 화면이 대상만 바꿔 이어지므로, 대상마다 따로 ViewModel을 둔다.
            detailViewModel = koinViewModel(key = "TagDetailViewModel:${navKey.id}") { parametersOf(navKey.id) },
            placeMapViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.tagMemoFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<TagMemoFinishedListNavKey>(
        metadata = { key ->
            ListDetailSceneStrategy.listPane(
                sceneKey = key,
                detailPlaceholder = { TagMemoFinishedListDetailPlaceholder() },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
        },
    ) { key ->
        TagMemoFinishedListScreen(
            navigateUp = backStack::navigateUpFromTagMemoFinishedList,
            navigateToMemoDetail = { id ->
                if (backStack.lastOrNull() is MemoDetailNavKey) {
                    backStack.removeLastOrNull()
                }

                backStack.add(MemoDetailNavKey(id = id))
            },
            memoViewModel = koinViewModel { parametersOf(key.tagId) },
            syncViewModel = koinViewModel(),
        )
    }
}

private fun NavBackStack<ScreenNavKey>.tagListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isTagListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = TagHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
