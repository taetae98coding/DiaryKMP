package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddScreen
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddViewModel
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeScreen
import io.github.taetae98coding.diary.feature.file.ui.picker.rememberFilePicker
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.fileEntry(backStack: NavBackStack<ScreenNavKey>) {
    fileHomeEntry(backStack = backStack)
    fileAddEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.fileHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileHomeNavKey> {
        FileHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { backStack.add(FileAddNavKey) },
            fileViewModel = koinViewModel(),
            uploadViewModel = koinViewModel(),
            refreshViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.fileAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileAddNavKey> {
        val viewModel = koinViewModel<FileAddViewModel>()

        FileAddScreen(
            navigateUp = backStack::removeLastOrNull,
            filePicker = rememberFilePicker(onPick = viewModel::select),
            viewModel = viewModel,
            accountViewModel = koinViewModel(),
        )
    }
}
