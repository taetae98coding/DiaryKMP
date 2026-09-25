package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_load_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_too_large_message
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileHomeScreen(
    navigateUp: () -> Unit,
    filePicker: FilePicker,
    fileViewModel: FileHomeViewModel,
    uploadViewModel: FileHomeUploadViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filePagingItems = fileViewModel.filePagingData.collectAsLazyPagingItems()
    val uiState by fileViewModel.uiState.collectAsStateWithLifecycle()
    val uploadUiState by uploadViewModel.uiState.collectAsStateWithLifecycle()

    UploadFileEffect(
        effect = uploadViewModel.effect,
        listState = listState,
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
    )

    RefreshFailedEffect(
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
    )

    FileHomeScaffold(
        onEvent = { event ->
            when (event) {
                is FileHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is FileHomeScaffoldEvent.ClickAdd -> {
                    if (!uploadUiState.isUploading) {
                        filePicker.open()
                    }
                }

                is FileHomeScaffoldEvent.ClickRetry -> {
                    filePagingItems.retry()
                }
            }
        },
        modifier = modifier,
        listState = listState,
        filePagingItems = filePagingItems,
        uiStateProvider = { uiState },
        uploadUiStateProvider = { uploadUiState },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun UploadFileEffect(
    effect: Flow<FileHomeUploadEffect>,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val tooLargeMessage = stringResource(Res.string.file_home_upload_too_large_message)
    val failedMessage = stringResource(Res.string.file_home_upload_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is FileHomeUploadEffect.UploadSucceeded -> {
                filePagingItems.refresh()
                // 목록은 첫 항목의 키를 따라 보던 자리를 지키므로, 올린 파일이 맨 앞에 나타난 뒤에 처음으로 옮겨야 그 파일이 보인다.
                // 첫 값은 다시 불러오기 전의 상태라 앞선 실패가 남아 있을 수 있으므로 건너뛴다.
                coroutineScope.launch {
                    val isUploadedFirst =
                        snapshotFlow { filePagingItems.itemSnapshotList.firstOrNull()?.id to filePagingItems.loadState.refresh }
                            .drop(1)
                            .first { (id, refresh) -> id == value.id || refresh is LoadState.Error }
                            .let { (id, _) -> id == value.id }

                    if (isUploadedFirst) listState.scrollToItem(index = 0)
                }
            }

            is FileHomeUploadEffect.UploadTooLarge -> {
                coroutineScope.launch { snackbarHostState.showImmediate(message = tooLargeMessage) }
            }

            is FileHomeUploadEffect.UploadFailed -> {
                coroutineScope.launch { snackbarHostState.showImmediate(message = failedMessage) }
            }
        }
    }
}

@Composable
private fun RefreshFailedEffect(
    filePagingItems: LazyPagingItems<DiaryFile>,
    snackbarHostState: SnackbarHostState,
) {
    val message by rememberUpdatedState(stringResource(Res.string.file_home_load_failed_message))

    LaunchedEffect(filePagingItems, snackbarHostState) {
        snapshotFlow { filePagingItems.loadState.refresh is LoadState.Error && filePagingItems.itemCount > 0 }
            .filter { isFailedWithItems -> isFailedWithItems }
            .collect { launch { snackbarHostState.showImmediate(message = message) } }
    }
}
