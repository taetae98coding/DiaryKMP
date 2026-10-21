@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.file.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.isFileAddOnDetailPane
import io.github.taetae98coding.diary.feature.file.api.isFileListDetailPane
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddAccountViewModel
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddScreen
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddViewModel
import io.github.taetae98coding.diary.feature.file.ui.detail.FileDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.file.ui.detail.FileDetailScreen
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeScaffoldComponentVisible
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
    entry<FileHomeNavKey>(
        clazzContentKey = { FILE_HOME_CONTENT_KEY },
        metadata = fileHomeListPaneMetadata(),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)
        FileHomeScreen(
            navigateUp = backStack::navigateUpFromFileHome,
            navigateToAdd = { backStack.add(FileAddNavKey) },
            navigateToDetail = { id -> backStack.navigateToFileDetail(id = id) },
            componentVisibleProvider = { FileHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isFileAddOnDetailPane()) },
            fileViewModel = koinViewModel(),
            uploadViewModel = koinViewModel(),
            refreshViewModel = koinViewModel(),
        )
    }
}

internal const val FILE_HOME_CONTENT_KEY: String = "FileHomeNavKey"

internal fun fileHomeListPaneMetadata(): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = FileHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = FILE_HOME_CONTENT_KEY) {
                FileAddDetailPlaceholder(
                    uploadViewModel = koinViewModel(),
                    accountViewModel = koinViewModel(),
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

// 고른 파일은 placeholder의 ViewModel에 남아 FileAdd가 화면에서 빠져도 지워지지 않으므로 따로 비운다.
@Composable
private fun FileAddDetailPlaceholder(
    uploadViewModel: FileAddViewModel,
    accountViewModel: FileAddAccountViewModel,
) {
    val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()

    if (accountUiState.isGuest) {
        LaunchedEffect(uploadViewModel) { uploadViewModel.clearSelectedFile() }
    } else {
        FileAddScreen(
            navigateUp = {},
            componentVisibleProvider = { FileAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
            filePicker = rememberFilePicker(onPick = uploadViewModel::select),
            uploadViewModel = uploadViewModel,
            accountViewModel = accountViewModel,
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.fileAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileAddNavKey>(
        metadata = { key -> backStack.fileListDetailPaneMetadata(key) },
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val uploadViewModel = koinViewModel<FileAddViewModel>()

        FileAddScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { FileAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            filePicker = rememberFilePicker(onPick = uploadViewModel::select),
            uploadViewModel = uploadViewModel,
            accountViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.fileDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<FileDetailNavKey>(
        metadata = { key -> backStack.fileListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        FileDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { FileDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            detailViewModel = koinViewModel { parametersOf(key.id) },
            accountViewModel = koinViewModel(),
        )
    }
}

private fun NavBackStack<ScreenNavKey>.fileListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isFileListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = FileHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
