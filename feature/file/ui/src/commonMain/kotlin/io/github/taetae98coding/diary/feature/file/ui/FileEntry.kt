package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeScreen

public fun EntryProviderScope<ScreenNavKey>.fileEntry(backStack: NavBackStack<ScreenNavKey>) {
    fileHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.fileHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileHomeNavKey> {
        FileHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
