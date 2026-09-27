package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_load_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_retry
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_too_large_message
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun FileHomeScreen(
    navigateUp: () -> Unit,
    filePicker: FilePicker,
    fileViewModel: FileHomeViewModel,
    uploadViewModel: FileHomeUploadViewModel,
    refreshViewModel: FileHomeRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filePagingItems = fileViewModel.filePagingData.collectAsLazyPagingItems()
    val uiState by fileViewModel.uiState.collectAsStateWithLifecycle()
    // Paging은 새 계정의 첫 페이지가 올 때까지 앞선 계정의 파일을 그대로 들고 있다.
    var isAccountChanging by remember { mutableStateOf(false) }
    val uploadUiState by uploadViewModel.uiState.collectAsStateWithLifecycle()
    val refreshUiState by refreshViewModel.uiState.collectAsStateWithLifecycle()
    // Paging은 불러오는 중에 다시 요청하면 진행 중이던 불러오기를 버리고 처음부터 다시 시작한다.
    val refresh = {
        when {
            filePagingItems.loadState.refresh is LoadState.Loading -> Unit
            filePagingItems.itemCount > 0 -> refreshViewModel.refresh()
            else -> filePagingItems.refresh()
        }
    }

    FileHomeEffect(
        uploadViewModel = uploadViewModel,
        refreshViewModel = refreshViewModel,
        uiStateProvider = { uiState },
        refreshUiStateProvider = { refreshUiState },
        listState = listState,
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
        refresh = refresh,
        onAccountChangingChange = { value -> isAccountChanging = value },
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

                is FileHomeScaffoldEvent.Refresh -> {
                    refresh()
                }
            }
        },
        modifier = modifier,
        listState = listState,
        filePagingItems = filePagingItems,
        uiStateProvider = { uiState },
        uploadUiStateProvider = { uploadUiState },
        isRefreshingProvider = { refreshUiState.isRefreshing },
        isAccountChangingProvider = { isAccountChanging },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun FileHomeEffect(
    uploadViewModel: FileHomeUploadViewModel,
    refreshViewModel: FileHomeRefreshViewModel,
    uiStateProvider: () -> FileHomeUiState,
    refreshUiStateProvider: () -> FileHomeRefreshUiState,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    snackbarHostState: SnackbarHostState,
    refresh: () -> Unit,
    onAccountChangingChange: (Boolean) -> Unit,
) {
    FileHomeViewingEffect(uploadViewModel = uploadViewModel)

    UploadFileEffect(
        effect = uploadViewModel.effect,
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
        refreshAfterUpload = refreshViewModel::refreshAfterUpload,
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
        onChangingChange = onAccountChangingChange,
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
    refreshAfterUpload: (firstFileIdBefore: Uuid) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val tooLargeMessage = stringResource(Res.string.file_home_upload_too_large_message)
    val failedMessage = stringResource(Res.string.file_home_upload_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is FileHomeUploadEffect.UploadSucceeded -> {
                val firstFile = filePagingItems.takeIf { items -> items.itemCount > 0 }?.peek(index = 0)

                if (firstFile != null && filePagingItems.loadState.refresh !is LoadState.Loading) {
                    refreshAfterUpload(firstFile.id)
                } else {
                    filePagingItems.refresh()
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
    onChangingChange: (Boolean) -> Unit,
) {
    val latestUiStateProvider by rememberUpdatedState(uiStateProvider)
    val latestOnChanged by rememberUpdatedState(onChanged)
    val latestOnChangingChange by rememberUpdatedState(onChangingChange)

    LaunchedEffect(listState, filePagingItems) {
        snapshotFlow { latestUiStateProvider().toListOwner() }
            .filterNotNull()
            .distinctUntilChanged()
            .drop(1)
            .collectLatest {
                val refreshBefore = filePagingItems.loadState.refresh
                val itemListBefore = filePagingItems.itemSnapshotList

                latestOnChanged()
                latestOnChangingChange(true)
                listState.requestScrollToItem(index = 0)
                try {
                    filePagingItems.awaitRefreshSettled(refreshBefore = refreshBefore, itemListBefore = itemListBefore)
                } finally {
                    latestOnChangingChange(false)
                }
            }
    }
}

private fun FileHomeUiState.toListOwner(): Any? =
    when (this) {
        is FileHomeUiState.Loading -> null
        is FileHomeUiState.Guest -> FileHomeUiState.Guest
        is FileHomeUiState.User -> accountId
    }
