package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeScreen

public fun EntryProviderScope<NavKey>.fileEntry(backStack: NavBackStack<NavKey>) {
    fileHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.fileHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<FileHomeNavKey> {
        FileHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
