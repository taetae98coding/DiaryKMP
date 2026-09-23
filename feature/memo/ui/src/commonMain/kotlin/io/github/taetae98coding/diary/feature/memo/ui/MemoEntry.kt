@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.findMemoDetailPaneListKey
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreen
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScreen
import io.github.taetae98coding.diary.feature.memo.ui.finished.MemoFinishedListScreen
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeScreen
import io.github.taetae98coding.diary.feature.memo.ui.home.ScrollToFirstMemoOnReselectEffect
import io.github.taetae98coding.diary.feature.memo.ui.home.filter.MemoHomeFilterContent
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.memoEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    memoHomeEntry(
        backStack = backStack,
        homeReselectEvent = homeReselectEvent,
    )
    memoHomeFilterEntry()
    memoFinishedListEntry(backStack = backStack)
    memoAddEntry(backStack = backStack)
    memoDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.memoHomeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    entry<MemoHomeNavKey>(
        metadata =
            ListDetailSceneStrategy.listPane(
                sceneKey = MemoHomeNavKey,
                detailPlaceholder = { MemoAddDetailPlaceholder(backStack = backStack) },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)
        val listState = rememberLazyListState()

        ScrollToFirstMemoOnReselectEffect(
            reselectEvent = homeReselectEvent,
            listState = listState,
        )
        MemoHomeScreen(
            navigateToAdd = {
                backStack.add(MemoAddNavKey())
            },
            navigateToFilter = {
                backStack.add(MemoHomeFilterNavKey)
            },
            navigateToFinishedList = {
                backStack.add(MemoFinishedListNavKey)
            },
            navigateToSearch = {
                backStack.add(SearchHomeNavKey(initialType = SearchHomeType.MEMO))
            },
            navigateToDetail = { id ->
                if (backStack.lastOrNull() is MemoDetailNavKey) {
                    backStack.removeLastOrNull()
                }

                backStack.add(MemoDetailNavKey(id))
            },
            componentVisibleProvider = {
                val isMemoDetailVisible = backStack.lastOrNull() is MemoDetailNavKey
                val isAddPaneVisible = isDetailPaneVisible && !isMemoDetailVisible

                MemoHomeScaffoldComponentVisible(isAddButtonVisible = !isAddPaneVisible)
            },
            listState = listState,
            memoViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoHomeFilterEntry() {
    entry<MemoHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        MemoHomeFilterContent()
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MemoFinishedListNavKey> {
        MemoFinishedListScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToDetail = { id -> backStack.add(MemoDetailNavKey(id)) },
            memoViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MemoAddNavKey>(
        metadata = { key -> backStack.memoListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        MemoAddScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id)) },
            navigateToWebAdd = { backStack.add(WebAddNavKey()) },
            navigateToWebDetail = { id -> backStack.add(WebDetailNavKey(id = id)) },
            navigateToContactAdd = { backStack.add(ContactAddNavKey) },
            navigateToContactDetail = { id -> backStack.add(ContactDetailNavKey(id = id)) },
            navigateToPlaceAdd = { coordinate ->
                backStack.add(
                    PlaceAddNavKey(
                        latitude = coordinate?.latitude,
                        longitude = coordinate?.longitude,
                    ),
                )
            },
            navigateToPlaceDetail = { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            initialDateRange = key.initialDateRange,
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { MemoAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            isStandalone = !isListPaneVisible,
            addViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.primaryTagId) },
            webViewModel = koinViewModel(),
            contactViewModel = koinViewModel(),
            placeViewModel = koinViewModel(),
            placeMapViewModel = koinViewModel(),
            geminiViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MemoDetailNavKey>(
        metadata = { key -> backStack.memoListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        MemoDetailScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToCopiedMemo = { id ->
                backStack.removeLastOrNull()
                backStack.add(MemoDetailNavKey(id))
            },
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id)) },
            navigateToWebAdd = { backStack.add(WebAddNavKey()) },
            navigateToWebDetail = { id -> backStack.add(WebDetailNavKey(id = id)) },
            navigateToContactAdd = { backStack.add(ContactAddNavKey) },
            navigateToContactDetail = { id -> backStack.add(ContactDetailNavKey(id = id)) },
            navigateToPlaceAdd = { coordinate ->
                backStack.add(
                    PlaceAddNavKey(
                        latitude = coordinate?.latitude,
                        longitude = coordinate?.longitude,
                    ),
                )
            },
            navigateToPlaceDetail = { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { MemoDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            isStandalone = !isListPaneVisible,
            detailViewModel = koinViewModel { parametersOf(key.id) },
            tagViewModel = koinViewModel { parametersOf(key.id) },
            webViewModel = koinViewModel { parametersOf(key.id) },
            contactViewModel = koinViewModel { parametersOf(key.id) },
            placeViewModel = koinViewModel { parametersOf(key.id) },
            placeMapViewModel = koinViewModel(),
            geminiViewModel = koinViewModel(),
        )
    }
}

private fun NavBackStack<ScreenNavKey>.memoListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> {
    val sceneKey = memoDetailPaneSceneKey(key) ?: return emptyMap()

    return ListDetailSceneStrategy.detailPane(sceneKey = sceneKey) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)
}

internal fun List<ScreenNavKey>.memoDetailPaneSceneKey(key: ScreenNavKey): ScreenNavKey? =
    findMemoDetailPaneListKey(key) { belowKey ->
        belowKey == MemoHomeNavKey || belowKey is TagMemoFinishedListNavKey
    }

@Composable
private fun MemoAddDetailPlaceholder(backStack: NavBackStack<ScreenNavKey>) {
    val tagAddRequestKey = rememberResultRequestKey()

    MemoAddScreen(
        navigateUp = {},
        navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
        navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id)) },
        navigateToWebAdd = { backStack.add(WebAddNavKey()) },
        navigateToWebDetail = { id -> backStack.add(WebDetailNavKey(id = id)) },
        navigateToContactAdd = { backStack.add(ContactAddNavKey) },
        navigateToContactDetail = { id -> backStack.add(ContactDetailNavKey(id = id)) },
        navigateToPlaceAdd = { coordinate ->
            backStack.add(
                PlaceAddNavKey(
                    latitude = coordinate?.latitude,
                    longitude = coordinate?.longitude,
                ),
            )
        },
        navigateToPlaceDetail = { id -> backStack.add(PlaceDetailNavKey(id = id)) },
        initialDateRange = null,
        tagAddRequestKey = tagAddRequestKey,
        componentVisibleProvider = { MemoAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
        isStandalone = false,
        addViewModel = koinViewModel(),
        tagViewModel = koinViewModel { parametersOf(null) },
        webViewModel = koinViewModel(),
        contactViewModel = koinViewModel(),
        placeViewModel = koinViewModel(),
        placeMapViewModel = koinViewModel(),
        geminiViewModel = koinViewModel(),
    )
}
