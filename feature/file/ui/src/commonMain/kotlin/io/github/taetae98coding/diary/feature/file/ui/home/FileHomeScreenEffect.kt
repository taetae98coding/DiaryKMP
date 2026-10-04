package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_load_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_retry
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_too_large_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun FileHomeScreenEffect(
    uploadViewModel: FileHomeUploadViewModel,
    refreshViewModel: FileHomeRefreshViewModel,
    fileViewModel: FileHomeViewModel,
    navigateToAdd: () -> Unit,
    uiStateProvider: () -> FileHomeUiState,
    refreshUiStateProvider: () -> FileHomeRefreshUiState,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    snackbarHostState: SnackbarHostState,
    refresh: () -> Unit,
) {
    FileHomeViewingEffect(uploadViewModel = uploadViewModel)

    UploadFileEffect(
        effect = uploadViewModel.effect,
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
        navigateToAdd = navigateToAdd,
        refreshAfterUpload = refreshViewModel::refreshAfterUpload,
        refreshAfterUploadOnFileAdd = refreshViewModel::refreshAfterUploadOnFileAdd,
    )

    RefreshFailedEffect(
        effect = refreshViewModel.effect,
        uiStateProvider = uiStateProvider,
        snackbarHostState = snackbarHostState,
        onRetry = refresh,
    )

    ScrollToTopAfterRefreshEffect(
        refreshUiStateProvider = refreshUiStateProvider,
        listState = listState,
        filePagingItems = filePagingItems,
        onScrolledToTop = refreshViewModel::onScrolledToTop,
    )

    AccountChangedEffect(
        uiStateProvider = uiStateProvider,
        listState = listState,
        filePagingItems = filePagingItems,
        onChanged = refreshViewModel::cancelRefresh,
        onChangeStart = fileViewModel::startAccountChange,
        onChangeFinish = fileViewModel::finishAccountChange,
    )
}

@Composable
private fun FileHomeViewingEffect(uploadViewModel: FileHomeUploadViewModel) {
    LifecycleStartEffect(uploadViewModel) {
        uploadViewModel.startViewing()

        onStopOrDispose { uploadViewModel.stopViewing() }
    }
}

@Composable
private fun UploadFileEffect(
    effect: Flow<FileHomeUploadEffect>,
    filePagingItems: LazyPagingItems<DiaryFile>,
    snackbarHostState: SnackbarHostState,
    navigateToAdd: () -> Unit,
    refreshAfterUpload: (firstFileIdBefore: Uuid) -> Unit,
    refreshAfterUploadOnFileAdd: (firstFileIdBefore: Uuid) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val tooLargeMessage = stringResource(Res.string.file_home_upload_too_large_message)
    val failedMessage = stringResource(Res.string.file_home_upload_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is FileHomeUploadEffect.UploadSucceeded -> {
                filePagingItems.refreshAfterUpload(refreshList = refreshAfterUpload)
            }

            is FileHomeUploadEffect.UploadSucceededOnFileAdd -> {
                filePagingItems.refreshAfterUpload(refreshList = refreshAfterUploadOnFileAdd)
            }

            is FileHomeUploadEffect.UploadTooLarge -> {
                coroutineScope.launch { snackbarHostState.showImmediate(message = tooLargeMessage) }
            }

            is FileHomeUploadEffect.UploadFailed -> {
                coroutineScope.launch { snackbarHostState.showImmediate(message = failedMessage) }
            }

            is FileHomeUploadEffect.NavigateToAdd -> {
                navigateToAdd()
            }
        }
    }
}

private fun LazyPagingItems<DiaryFile>.refreshAfterUpload(refreshList: (firstFileIdBefore: Uuid) -> Unit) {
    val firstFile = takeIf { items -> items.itemCount > 0 }?.peek(index = 0)

    if (firstFile != null && loadState.refresh !is LoadState.Loading) {
        refreshList(firstFile.id)
    } else {
        refresh()
    }
}

@Composable
private fun RefreshFailedEffect(
    effect: Flow<FileHomeRefreshEffect>,
    uiStateProvider: () -> FileHomeUiState,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val message = stringResource(Res.string.file_home_load_failed_message)
    val actionLabel = stringResource(Res.string.file_home_retry)
    val latestUiStateProvider by rememberUpdatedState(uiStateProvider)
    val latestOnRetry by rememberUpdatedState(onRetry)

    CollectEffect(effect) { value ->
        when (value) {
            is FileHomeRefreshEffect.RefreshFailed -> {
                if (latestUiStateProvider() is FileHomeUiState.User) {
                    coroutineScope.launch {
                        val result = snackbarHostState.showImmediate(message = message, actionLabel = actionLabel)

                        if (result == SnackbarResult.ActionPerformed) latestOnRetry()
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollToTopAfterRefreshEffect(
    refreshUiStateProvider: () -> FileHomeRefreshUiState,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    onScrolledToTop: () -> Unit,
) {
    val latestRefreshUiStateProvider by rememberUpdatedState(refreshUiStateProvider)
    val latestOnScrolledToTop by rememberUpdatedState(onScrolledToTop)

    LaunchedEffect(listState, filePagingItems) {
        snapshotFlow { latestRefreshUiStateProvider().scrollToTop to filePagingItems.itemSnapshotList.firstOrNull()?.id }
            .filter { (scrollToTop, firstFileId) ->
                scrollToTop is FileHomeScrollToTop.AfterFirstFileChanges && firstFileId != null && firstFileId != scrollToTop.firstFileIdBefore
            }.collect {
                listState.scrollToItem(index = 0)
                latestOnScrolledToTop()
            }
    }
}

// 불러오는 중이 한 프레임 안에 끝나면 snapshotFlow가 그 상태를 건너뛰므로, 결과가 달라진 것으로도 끝났다고 본다.
private suspend fun LazyPagingItems<DiaryFile>.awaitRefreshSettled(
    refreshBefore: LoadState,
    itemListBefore: Any,
): LoadState {
    var isLoadingSeen = false

    return snapshotFlow { loadState.refresh to itemSnapshotList }
        .first { (refresh, itemList) ->
            if (refresh is LoadState.Loading) {
                isLoadingSeen = true
                false
            } else {
                isLoadingSeen || refresh !== refreshBefore || itemList !== itemListBefore
            }
        }.first
}

// 목록의 자리는 계정과 무관하게 화면이 들고 있어 계정이 바뀌어도 그대로 남는다. 처음 읽는 계정은 되살린 화면의 계정일 수 있어 건너뛴다.
@Composable
private fun AccountChangedEffect(
    uiStateProvider: () -> FileHomeUiState,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    onChanged: () -> Unit,
    onChangeStart: () -> Unit,
    onChangeFinish: () -> Unit,
) {
    val latestUiStateProvider by rememberUpdatedState(uiStateProvider)
    val latestOnChanged by rememberUpdatedState(onChanged)
    val latestOnChangeStart by rememberUpdatedState(onChangeStart)
    val latestOnChangeFinish by rememberUpdatedState(onChangeFinish)

    LaunchedEffect(listState, filePagingItems) {
        snapshotFlow { latestUiStateProvider() }
            .filter { uiState -> uiState !is FileHomeUiState.Loading }
            .map { uiState -> uiState.toListOwner() }
            .distinctUntilChanged()
            .drop(1)
            .collectLatest {
                val refreshBefore = filePagingItems.loadState.refresh
                val itemListBefore = filePagingItems.itemSnapshotList

                latestOnChanged()
                latestOnChangeStart()
                listState.requestScrollToItem(index = 0)
                try {
                    filePagingItems.awaitRefreshSettled(refreshBefore = refreshBefore, itemListBefore = itemListBefore)
                } finally {
                    latestOnChangeFinish()
                }
            }
    }
}

private fun FileHomeUiState.toListOwner(): Any =
    when (this) {
        is FileHomeUiState.Loading -> FileHomeUiState.Loading
        is FileHomeUiState.Guest -> FileHomeUiState.Guest
        is FileHomeUiState.User -> accountId
    }
