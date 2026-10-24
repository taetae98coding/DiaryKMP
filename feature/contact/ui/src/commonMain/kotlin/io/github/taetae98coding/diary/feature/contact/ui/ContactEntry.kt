@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.contact.api.isContactAddOnDetailPane
import io.github.taetae98coding.diary.feature.contact.api.isContactListDetailPane
import io.github.taetae98coding.diary.feature.contact.ui.add.ContactAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.add.ContactAddScreen
import io.github.taetae98coding.diary.feature.contact.ui.contact_memo_finished_list_empty_title
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeScreen
import io.github.taetae98coding.diary.feature.contact.ui.memo.finished.ContactMemoFinishedListViewModel
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListDetailPlaceholder
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListScreen
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.contactEntry(backStack: NavBackStack<ScreenNavKey>) {
    contactHomeEntry(backStack = backStack)
    contactAddEntry(backStack = backStack)
    contactDetailEntry(backStack = backStack)
    contactMemoFinishedListEntry(backStack = backStack)
}

internal fun EntryProviderScope<ScreenNavKey>.contactHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ContactHomeNavKey>(
        clazzContentKey = { CONTACT_HOME_CONTENT_KEY },
        metadata = contactHomeListPaneMetadata(),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        ContactHomeScreen(
            navigateUp = backStack::navigateUpFromContactHome,
            navigateToAdd = { backStack.add(ContactAddNavKey()) },
            navigateToDetail = { id -> backStack.navigateToContactDetail(id) },
            componentVisibleProvider = { ContactHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isContactAddOnDetailPane()) },
            contactViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal const val CONTACT_HOME_CONTENT_KEY: String = "ContactHomeNavKey"

internal fun contactHomeListPaneMetadata(): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = ContactHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = CONTACT_HOME_CONTENT_KEY) {
                ContactAddScreen(
                    navigateUp = {},
                    addedResultRequestKey = null,
                    componentVisibleProvider = { ContactAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    viewModel = koinViewModel(),
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

internal fun EntryProviderScope<ScreenNavKey>.contactAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ContactAddNavKey>(
        metadata = { key -> backStack.contactListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        ContactAddScreen(
            navigateUp = backStack::removeLastOrNull,
            addedResultRequestKey = key.requestKey,
            componentVisibleProvider = { ContactAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel(),
        )
    }
}

internal fun EntryProviderScope<ScreenNavKey>.contactDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ContactDetailNavKey>(
        metadata = { key -> backStack.contactListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        ContactDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToMemoAdd = { backStack.add(MemoAddNavKey(initialContactId = key.id)) },
            navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id = id)) },
            navigateToMemoFinishedList = { backStack.add(ContactMemoFinishedListNavKey(contactId = key.id)) },
            id = key.id,
            componentVisibleProvider = { ContactDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.contactListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isContactListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = ContactHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }

private fun EntryProviderScope<ScreenNavKey>.contactMemoFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ContactMemoFinishedListNavKey>(
        metadata = ::contactMemoFinishedListPaneMetadata,
    ) { key ->
        EntityMemoFinishedListScreen(
            navigateUp = backStack::navigateUpFromContactMemoFinishedList,
            navigateToMemoDetail = backStack::navigateToMemoDetail,
            emptyTitle = stringResource(Res.string.contact_memo_finished_list_empty_title),
            memoViewModel = koinViewModel<ContactMemoFinishedListViewModel> { parametersOf(key.contactId) },
            syncViewModel = koinViewModel(),
        )
    }
}

internal fun contactMemoFinishedListPaneMetadata(key: ContactMemoFinishedListNavKey): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = key,
        detailPlaceholder = { EntityMemoFinishedListDetailPlaceholder() },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

internal fun NavBackStack<ScreenNavKey>.navigateUpFromContactMemoFinishedList() {
    val index = indexOfLast { key -> key is ContactMemoFinishedListNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
