package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddScreen
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddViewModel
import io.github.taetae98coding.diary.feature.file.ui.detail.FileDetailScreen
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeScreen
import io.github.taetae98coding.diary.feature.file.ui.picker.rememberFilePicker
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.fileEntry(backStack: NavBackStack<ScreenNavKey>) {
    fileHomeEntry(backStack = backStack)
    fileAddEntry(backStack = backStack)
    fileDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.fileHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileHomeNavKey> {
        FileHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { backStack.add(FileAddNavKey) },
            navigateToDetail = { id -> backStack.add(FileDetailNavKey(id = id)) },
            fileViewModel = koinViewModel(),
            uploadViewModel = koinViewModel(),
            refreshViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.fileAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileAddNavKey> {
        val uploadViewModel = koinViewModel<FileAddViewModel>()

        FileAddScreen(
            navigateUp = backStack::removeLastOrNull,
            filePicker = rememberFilePicker(onPick = uploadViewModel::select),
            uploadViewModel = uploadViewModel,
            accountViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.fileDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileDetailNavKey> { key ->
        FileDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            detailViewModel = koinViewModel { parametersOf(key.id) },
            accountViewModel = koinViewModel(),
        )
    }
}
