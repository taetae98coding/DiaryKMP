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
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
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
import kotlin.uuid.Uuid

@Composable
internal fun FileHomeScaffold(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filePagingItems: LazyPagingItems<DiaryFile> = remember { flowOf(PagingData.empty<DiaryFile>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> FileHomeUiState = { FileHomeUiState.Loading },
    uploadUiStateProvider: () -> FileHomeUploadUiState = { FileHomeUploadUiState() },
    isRefreshingProvider: () -> Boolean = { false },
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
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
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
            contentKey = { uiState -> uiState::class },
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
                    FileHomeGuestBox(modifier = Modifier.fillMaxSize())
                }

                is FileHomeUiState.User -> {
                    FileHomeUserBody(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        listState = listState,
                        filePagingItems = filePagingItems,
                        isRefreshingProvider = isRefreshingProvider,
                        isAccountChangingProvider = { (uiStateProvider() as? FileHomeUiState.User)?.isAccountChanging == true },
                    )
                }
            }
        }
    }
}

@Composable
private fun FileHomeGuestBox(modifier: Modifier = Modifier) {
    DiaryEmptyBox(
        title = stringResource(Res.string.file_home_guest_title),
        modifier = modifier,
        description = stringResource(Res.string.file_home_guest_description),
        icon = { FileIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    )
}

@Composable
private fun FileHomeUserBody(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filePagingItems: LazyPagingItems<DiaryFile> = remember { flowOf(PagingData.empty<DiaryFile>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    isAccountChangingProvider: () -> Boolean = { false },
) {
    DiaryPullToRefreshBox(
        onRefresh = { onEvent(FileHomeScaffoldEvent.Refresh) },
        modifier = modifier,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        FileHomeList(
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize(),
            listState = listState,
            filePagingItems = filePagingItems,
            isAccountChangingProvider = isAccountChangingProvider,
        )
    }
}

private data class FileHomeScaffoldPreviewState(
    val uiState: FileHomeUiState,
    val isUploading: Boolean = false,
)

private class FileHomeScaffoldPreviewStatePreviewParameter : PreviewParameterProvider<FileHomeScaffoldPreviewState> {
    override val values: Sequence<FileHomeScaffoldPreviewState> =
        sequenceOf(
            FileHomeScaffoldPreviewState(uiState = FileHomeUiState.Loading),
            FileHomeScaffoldPreviewState(uiState = FileHomeUiState.Guest),
            FileHomeScaffoldPreviewState(uiState = FileHomeUiState.User(accountId = Uuid.NIL)),
            FileHomeScaffoldPreviewState(uiState = FileHomeUiState.User(accountId = Uuid.NIL), isUploading = true),
        )
}

@ScreenPreview
@Composable
private fun FileHomeScaffoldPreview(
    @PreviewParameter(FileHomeScaffoldPreviewStatePreviewParameter::class) previewState: FileHomeScaffoldPreviewState,
) {
    val filePagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewDiaryFile(title = "분기 보고서", name = "보고서.pdf", size = 24_536_679),
                        previewDiaryFile(title = "회의록", name = "memo.txt", size = 512),
                    ),
                ),
            )
        }

    DiaryTheme {
        FileHomeScaffold(
            onEvent = {},
            filePagingItems = filePagingData.collectAsLazyPagingItems(),
            uiStateProvider = { previewState.uiState },
            uploadUiStateProvider = { FileHomeUploadUiState(isUploading = previewState.isUploading) },
        )
    }
}
