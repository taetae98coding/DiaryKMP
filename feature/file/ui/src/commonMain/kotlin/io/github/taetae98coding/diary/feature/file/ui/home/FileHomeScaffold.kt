package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FileIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_add_button_content_description
import io.github.taetae98coding.diary.feature.file.ui.file_home_guest_description
import io.github.taetae98coding.diary.feature.file.ui.file_home_guest_title
import io.github.taetae98coding.diary.feature.file.ui.file_home_title
import io.github.taetae98coding.diary.feature.file.ui.file_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileHomeScaffold(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filePagingItems: LazyPagingItems<DiaryFile> = remember { flowOf(PagingData.empty<DiaryFile>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> FileHomeUiState = { FileHomeUiState.Loading },
    uploadUiStateProvider: () -> FileHomeUploadUiState = { FileHomeUploadUiState() },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.file_home_title),
                onNavigateUp = { onEvent(FileHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.file_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiStateProvider() is FileHomeUiState.User) {
                FloatingAddButton(
                    onClick = { onEvent(FileHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.file_home_add_button_content_description),
                    isInProgressProvider = { uploadUiStateProvider().isUploading },
                )
            }
        },
    ) { paddingValues ->
        DiaryCrossfade(
            targetState = uiStateProvider(),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) { uiState ->
            when (uiState) {
                is FileHomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize())
                }

                is FileHomeUiState.Guest -> {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.file_home_guest_title),
                        modifier = Modifier.fillMaxSize(),
                        description = stringResource(Res.string.file_home_guest_description),
                        icon = { FileIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                }

                is FileHomeUiState.User -> {
                    FileHomeList(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        listState = listState,
                        filePagingItems = filePagingItems,
                    )
                }
            }
        }
    }
}

private class FileHomeUiStatePreviewParameter : PreviewParameterProvider<FileHomeUiState> {
    override val values: Sequence<FileHomeUiState> =
        sequenceOf(
            FileHomeUiState.Loading,
            FileHomeUiState.Guest,
            FileHomeUiState.User,
        )
}

@ScreenPreview
@Composable
private fun FileHomeScaffoldPreview(
    @PreviewParameter(FileHomeUiStatePreviewParameter::class) uiState: FileHomeUiState,
) {
    val filePagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewDiaryFile(name = "보고서.pdf", size = 24_536_679),
                        previewDiaryFile(name = "memo.txt", size = 512),
                    ),
                ),
            )
        }

    DiaryTheme {
        FileHomeScaffold(
            onEvent = {},
            filePagingItems = filePagingData.collectAsLazyPagingItems(),
            uiStateProvider = { uiState },
        )
    }
}
