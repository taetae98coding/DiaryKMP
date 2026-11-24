@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.findMemoDetailPaneListKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreen
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.initialDateTime
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScreen
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailViewModel
import io.github.taetae98coding.diary.feature.memo.ui.finished.MemoFinishedListScreen
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeScreen
import io.github.taetae98coding.diary.feature.memo.ui.home.ScrollToFirstMemoOnReselectEffect
import io.github.taetae98coding.diary.feature.memo.ui.home.filter.MemoHomeFilterContent
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import kotlinx.coroutines.flow.Flow

internal fun EntryProviderScope<ScreenNavKey>.memoEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
    contentSharerFactory: ContentSharerFactory,
) {
    memoHomeEntry(
        backStack = backStack,
        homeReselectEvent = homeReselectEvent,
    )
    memoHomeFilterEntry(backStack = backStack)
    memoFinishedListEntry(backStack = backStack)
    memoAddEntry(backStack = backStack)
    memoDetailEntry(backStack = backStack, contentSharerFactory = contentSharerFactory)
}

private fun EntryProviderScope<ScreenNavKey>.memoHomeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    entry<MemoHomeNavKey>(
        clazzContentKey = { MEMO_HOME_CONTENT_KEY },
        metadata = memoHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)
        val listState = rememberLazyListState()

        ScrollToFirstMemoOnReselectEffect(
            reselectEvent = homeReselectEvent,
            listState = listState,
        )
        MemoHomeScreen(
            navigateToAdd = dropUnlessResumed(block = backStack::navigateToMemoAddFromHome),
            navigateToFilter =
                dropUnlessResumed {
                    backStack.add(MemoHomeFilterNavKey)
                },
            navigateToFinishedList =
                dropUnlessResumed {
                    backStack.add(MemoFinishedListNavKey)
                },
            navigateToSearch = dropUnlessResumed(block = backStack::navigateToSearchFromMemoHome),
            navigateToDetail = { id -> backStack.navigateToMemoDetail(id) },
            componentVisibleProvider = {
                val isAddPaneVisible = isDetailPaneVisible && !backStack.isMemoDetailOnDetailPane()

                MemoHomeScaffoldComponentVisible(isAddButtonVisible = !isAddPaneVisible)
            },
            listState = listState,
            memoViewModel = metroViewModel(),
            syncViewModel = metroViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoHomeFilterEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MemoHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        MemoHomeFilterContent(
            navigateToTagAdd = backStack::navigateToTagAddFromFilter,
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MemoFinishedListNavKey>(
        metadata = memoFinishedListPaneMetadata(),
    ) {
        MemoFinishedListScreen(
            navigateUp = backStack::navigateUpFromMemoFinishedList,
            navigateToDetail = backStack::navigateToMemoDetail,
            memoViewModel = metroViewModel(),
            syncViewModel = metroViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MemoAddNavKey>(
        metadata = { key -> backStack.memoListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val addRequestKey = rememberResultRequestKey()

        MemoAddScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = addRequestKey)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id)) },
            navigateToWebAdd = dropUnlessResumed { backStack.navigateToWebAddFromMemoWebInput(requestKey = addRequestKey) },
            navigateToWebDetail = dropUnlessResumedWithArg { id -> backStack.add(WebDetailNavKey(id = id)) },
            navigateToContactAdd = dropUnlessResumed { backStack.navigateToContactAddFromMemoContactInput(requestKey = addRequestKey) },
            navigateToContactDetail = dropUnlessResumedWithArg { id -> backStack.add(ContactDetailNavKey(id = id)) },
            navigateToPlaceAdd =
                dropUnlessResumedWithArg { coordinate ->
                    backStack.add(
                        PlaceAddNavKey(
                            latitude = coordinate?.latitude,
                            longitude = coordinate?.longitude,
                            requestKey = addRequestKey,
                        ),
                    )
                },
            navigateToPlaceDetail = dropUnlessResumedWithArg { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            navigateToGeminiSetting = { backStack.add(SettingGeminiNavKey) },
            initialTitle = key.initialTitle,
            initialDescription = key.initialDescription,
            initialDateTime = key.initialDateTime(),
            addRequestKey = addRequestKey,
            componentVisibleProvider = { MemoAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            isStandalone = !isListPaneVisible,
            addViewModel = metroViewModel(),
            tagViewModel = assistedMetroViewModel<MemoAddTagViewModel, MemoAddTagViewModel.Factory> { create(initialPrimaryTagId = key.primaryTagId) },
            webViewModel = assistedMetroViewModel<MemoAddWebViewModel, MemoAddWebViewModel.Factory> { create(initialWebId = key.initialWebId) },
            contactViewModel = assistedMetroViewModel<MemoAddContactViewModel, MemoAddContactViewModel.Factory> { create(initialContactId = key.initialContactId) },
            placeViewModel = assistedMetroViewModel<MemoAddPlaceViewModel, MemoAddPlaceViewModel.Factory> { create(initialPlaceId = key.initialPlaceId) },
            placeMapViewModel = metroViewModel(),
            placePickerMapViewModel = metroViewModel(),
            geminiViewModel = metroViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.memoDetailEntry(
    backStack: NavBackStack<ScreenNavKey>,
    contentSharerFactory: ContentSharerFactory,
) {
    entry<MemoDetailNavKey>(
        metadata = { key -> backStack.memoListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val addRequestKey = rememberResultRequestKey()

        MemoDetailScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToCopiedMemo = { id -> backStack.navigateToCopiedMemo(id) },
            navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = addRequestKey)) },
            navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id)) },
            navigateToWebAdd = dropUnlessResumed { backStack.navigateToWebAddFromMemoWebInput(requestKey = addRequestKey) },
            navigateToWebDetail = dropUnlessResumedWithArg { id -> backStack.add(WebDetailNavKey(id = id)) },
            navigateToContactAdd = dropUnlessResumed { backStack.navigateToContactAddFromMemoContactInput(requestKey = addRequestKey) },
            navigateToContactDetail = dropUnlessResumedWithArg { id -> backStack.add(ContactDetailNavKey(id = id)) },
            navigateToPlaceAdd =
                dropUnlessResumedWithArg { coordinate ->
                    backStack.add(
                        PlaceAddNavKey(
                            latitude = coordinate?.latitude,
                            longitude = coordinate?.longitude,
                            requestKey = addRequestKey,
                        ),
                    )
                },
            navigateToPlaceDetail = dropUnlessResumedWithArg { id -> backStack.add(PlaceDetailNavKey(id = id)) },
            navigateToGeminiSetting = { backStack.add(SettingGeminiNavKey) },
            addRequestKey = addRequestKey,
            componentVisibleProvider = { MemoDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            isStandalone = !isListPaneVisible,
            detailViewModel = assistedMetroViewModel<MemoDetailViewModel, MemoDetailViewModel.Factory> { create(id = key.id) },
            tagViewModel = assistedMetroViewModel<MemoTagViewModel, MemoTagViewModel.Factory> { create(id = key.id) },
            webViewModel = assistedMetroViewModel<MemoWebViewModel, MemoWebViewModel.Factory> { create(id = key.id) },
            contactViewModel = assistedMetroViewModel<MemoContactViewModel, MemoContactViewModel.Factory> { create(id = key.id) },
            placeViewModel = assistedMetroViewModel<MemoPlaceViewModel, MemoPlaceViewModel.Factory> { create(id = key.id) },
            placeMapViewModel = metroViewModel(),
            placePickerMapViewModel = metroViewModel(),
            geminiViewModel = metroViewModel(),
            contentSharerFactory = contentSharerFactory,
        )
    }
}

private fun NavBackStack<ScreenNavKey>.memoListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> {
    val sceneKey = memoDetailPaneSceneKey(key) ?: return emptyMap()

    return ListDetailSceneStrategy.detailPane(sceneKey = sceneKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
}

internal const val MEMO_HOME_CONTENT_KEY: String = "MemoHomeNavKey"

internal fun memoHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = MemoHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = MEMO_HOME_CONTENT_KEY) {
                MemoAddDetailPlaceholder(backStack = backStack)
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

internal fun List<ScreenNavKey>.isMemoDetailOnDetailPane(): Boolean = lastOrNull { key -> key != MemoHomeFilterNavKey } is MemoDetailNavKey

internal fun List<ScreenNavKey>.memoDetailPaneSceneKey(key: ScreenNavKey): ScreenNavKey? =
    findMemoDetailPaneListKey(key) { belowKey ->
        belowKey == MemoHomeNavKey ||
            belowKey == MemoFinishedListNavKey ||
            belowKey is TagMemoFinishedListNavKey ||
            belowKey is ContactMemoFinishedListNavKey ||
            belowKey is PlaceMemoFinishedListNavKey ||
            belowKey is WebMemoFinishedListNavKey
    }

@Composable
private fun MemoAddDetailPlaceholder(backStack: NavBackStack<ScreenNavKey>) {
    val addRequestKey = rememberResultRequestKey()

    MemoAddScreen(
        navigateUp = {},
        navigateToTagAdd = dropUnlessResumed { backStack.add(TagAddNavKey(requestKey = addRequestKey)) },
        navigateToTagDetail = dropUnlessResumedWithArg { id -> backStack.add(TagDetailNavKey(id)) },
        navigateToWebAdd = dropUnlessResumed { backStack.navigateToWebAddFromMemoWebInput(requestKey = addRequestKey) },
        navigateToWebDetail = dropUnlessResumedWithArg { id -> backStack.add(WebDetailNavKey(id = id)) },
        navigateToContactAdd = dropUnlessResumed { backStack.navigateToContactAddFromMemoContactInput(requestKey = addRequestKey) },
        navigateToContactDetail = dropUnlessResumedWithArg { id -> backStack.add(ContactDetailNavKey(id = id)) },
        navigateToPlaceAdd =
            dropUnlessResumedWithArg { coordinate ->
                backStack.add(
                    PlaceAddNavKey(
                        latitude = coordinate?.latitude,
                        longitude = coordinate?.longitude,
                        requestKey = addRequestKey,
                    ),
                )
            },
        navigateToPlaceDetail = dropUnlessResumedWithArg { id -> backStack.add(PlaceDetailNavKey(id = id)) },
        navigateToGeminiSetting = { backStack.add(SettingGeminiNavKey) },
        initialTitle = "",
        initialDescription = "",
        initialDateTime = null,
        addRequestKey = addRequestKey,
        componentVisibleProvider = { MemoAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
        isStandalone = false,
        addViewModel = metroViewModel(),
        tagViewModel = assistedMetroViewModel<MemoAddTagViewModel, MemoAddTagViewModel.Factory> { create(initialPrimaryTagId = null) },
        webViewModel = assistedMetroViewModel<MemoAddWebViewModel, MemoAddWebViewModel.Factory> { create(initialWebId = null) },
        contactViewModel = assistedMetroViewModel<MemoAddContactViewModel, MemoAddContactViewModel.Factory> { create(initialContactId = null) },
        placeViewModel = assistedMetroViewModel<MemoAddPlaceViewModel, MemoAddPlaceViewModel.Factory> { create(initialPlaceId = null) },
        placeMapViewModel = metroViewModel(),
        placePickerMapViewModel = metroViewModel(),
        geminiViewModel = metroViewModel(),
    )
}
