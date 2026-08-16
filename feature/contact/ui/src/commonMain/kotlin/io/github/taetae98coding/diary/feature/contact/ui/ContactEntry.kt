@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.isContactListDetailPane
import io.github.taetae98coding.diary.feature.contact.ui.add.ContactAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.add.ContactAddScreen
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailScreen
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<NavKey>.contactEntry(backStack: NavBackStack<NavKey>) {
    contactHomeEntry(backStack = backStack)
    contactAddEntry(backStack = backStack)
    contactDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.contactHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<ContactHomeNavKey>(
        metadata =
            ListDetailSceneStrategy.listPane(
                sceneKey = ContactHomeNavKey,
                detailPlaceholder = {
                    ContactAddScreen(
                        navigateUp = {},
                        componentVisibleProvider = { ContactAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                        viewModel = koinViewModel(),
                    )
                },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        ContactHomeScreen(
            navigateUp = backStack::navigateUpFromContactHome,
            navigateToAdd = { backStack.add(ContactAddNavKey) },
            navigateToDetail = { id -> backStack.add(ContactDetailNavKey(id = id)) },
            componentVisibleProvider = { ContactHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible) },
            contactViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<NavKey>.contactAddEntry(backStack: NavBackStack<NavKey>) {
    entry<ContactAddNavKey>(
        metadata = { key -> backStack.contactListDetailPaneMetadata(key) },
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        ContactAddScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { ContactAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<NavKey>.contactDetailEntry(backStack: NavBackStack<NavKey>) {
    entry<ContactDetailNavKey>(
        metadata = { key -> backStack.contactListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        ContactDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { ContactDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            viewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}

private fun NavBackStack<NavKey>.contactListDetailPaneMetadata(key: NavKey): Map<String, Any> =
    if (isContactListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = ContactHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)
    } else {
        emptyMap()
    }
