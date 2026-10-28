package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_upload_too_large_message
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileHomeScreenEffect(
    uploadViewModel: FileHomeUploadViewModel,
    navigateToAdd: () -> Unit,
    uiStateProvider: () -> FileHomeUiState,
    componentVisibleProvider: () -> FileHomeScaffoldComponentVisible,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    snackbarHostState: SnackbarHostState,
    onAccountChangeStart: () -> Unit,
    onAccountChangeFinish: () -> Unit,
) {
    var isScrollToTopPending by rememberSaveable { mutableStateOf(false) }
    var refreshBefore by remember { mutableStateOf<FileHomeRefreshSnapshot?>(null) }

    UploadFileEffect(
        uploadViewModel = uploadViewModel,
        // 목록과 함께 FileAdd가 보이면 결과는 FileAdd가 알린다.
        isResultVisibleProvider = { componentVisibleProvider().isAddButtonVisible },
        snackbarHostState = snackbarHostState,
        navigateToAdd = navigateToAdd,
        onSucceeded = {
            refreshBefore = filePagingItems.refreshSnapshot()
            filePagingItems.refresh()
            isScrollToTopPending = true
        },
    )

    if (isScrollToTopPending) {
        ScrollToTopAfterRefreshEffect(
            listState = listState,
            filePagingItems = filePagingItems,
            refreshBeforeProvider = { refreshBefore },
            onFinished = {
                refreshBefore = null
                isScrollToTopPending = false
            },
        )
    }

    AccountChangedEffect(
        uiStateProvider = uiStateProvider,
        listState = listState,
        filePagingItems = filePagingItems,
        onChangeStart = onAccountChangeStart,
        onChangeFinish = onAccountChangeFinish,
    )
}

@Composable
private fun UploadFileEffect(
    uploadViewModel: FileHomeUploadViewModel,
    isResultVisibleProvider: () -> Boolean,
    snackbarHostState: SnackbarHostState,
    navigateToAdd: () -> Unit,
    onSucceeded: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val tooLargeMessage = stringResource(Res.string.file_home_upload_too_large_message)
    val failedMessage = stringResource(Res.string.file_home_upload_failed_message)

    CollectEffect(uploadViewModel.effect) { value ->
        when (value) {
            is FileHomeUploadEffect.UploadSucceeded -> {
                onSucceeded()
            }

            is FileHomeUploadEffect.UploadTooLarge -> {
                if (isResultVisibleProvider()) coroutineScope.launch { snackbarHostState.showImmediate(message = tooLargeMessage) }
            }

            is FileHomeUploadEffect.UploadFailed -> {
                if (isResultVisibleProvider()) coroutineScope.launch { snackbarHostState.showImmediate(message = failedMessage) }
            }

            is FileHomeUploadEffect.NavigateToAdd -> {
                navigateToAdd()
            }
        }
    }
}

private class FileHomeRefreshSnapshot(
    val refresh: LoadState,
    val itemList: Any,
)

private fun LazyPagingItems<DiaryFile>.refreshSnapshot(): FileHomeRefreshSnapshot = FileHomeRefreshSnapshot(refresh = loadState.refresh, itemList = itemSnapshotList)

// 새 목록이 나타나기 전에 처음으로 옮기면 LazyList가 앞선 첫 항목의 키를 따라 그 자리로 되돌아가므로, 다시 불러오기가 끝난 뒤에 옮긴다.
// 화면이 다시 만들어져 다시 불러오기 전의 상태를 모르면 지금 상태에서부터 끝나기를 기다린다.
@Composable
private fun ScrollToTopAfterRefreshEffect(
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    refreshBeforeProvider: () -> FileHomeRefreshSnapshot?,
    onFinished: () -> Unit,
) {
    val latestOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(listState, filePagingItems) {
        val refreshBefore = refreshBeforeProvider() ?: filePagingItems.refreshSnapshot()
        val refresh = filePagingItems.awaitRefreshSettled(refreshBefore = refreshBefore)

        if (refresh is LoadState.NotLoading) listState.scrollToItem(index = 0)
        latestOnFinished()
    }
}

// 불러오는 중이 한 프레임 안에 끝나면 snapshotFlow가 그 상태를 건너뛰므로, 결과가 달라진 것으로도 끝났다고 본다.
private suspend fun LazyPagingItems<DiaryFile>.awaitRefreshSettled(refreshBefore: FileHomeRefreshSnapshot): LoadState {
    var isLoadingSeen = false

    return snapshotFlow { loadState.refresh to itemSnapshotList }
        .first { (refresh, itemList) ->
            if (refresh is LoadState.Loading) {
                isLoadingSeen = true
                false
            } else {
                isLoadingSeen || refresh !== refreshBefore.refresh || itemList !== refreshBefore.itemList
            }
        }.first
}

// 목록의 자리는 계정과 무관하게 화면이 들고 있어 계정이 바뀌어도 그대로 남는다. 처음 읽는 계정은 되살린 화면의 계정일 수 있어 건너뛴다.
// Paging은 새 계정의 첫 페이지가 올 때까지 앞선 계정의 파일을 들고 있으므로 그동안 목록을 가린다.
@Composable
private fun AccountChangedEffect(
    uiStateProvider: () -> FileHomeUiState,
    listState: LazyListState,
    filePagingItems: LazyPagingItems<DiaryFile>,
    onChangeStart: () -> Unit,
    onChangeFinish: () -> Unit,
) {
    val latestUiStateProvider by rememberUpdatedState(uiStateProvider)
    val latestOnChangeStart by rememberUpdatedState(onChangeStart)
    val latestOnChangeFinish by rememberUpdatedState(onChangeFinish)

    LaunchedEffect(listState, filePagingItems) {
        snapshotFlow { latestUiStateProvider() }
            .filter { uiState -> uiState !is FileHomeUiState.Loading }
            .map { uiState -> (uiState as? FileHomeUiState.User)?.accountId }
            .distinctUntilChanged()
            .drop(1)
            .collectLatest {
                val refreshBefore = filePagingItems.refreshSnapshot()

                latestOnChangeStart()
                listState.requestScrollToItem(index = 0)
                try {
                    filePagingItems.awaitRefreshSettled(refreshBefore = refreshBefore)
                } finally {
                    latestOnChangeFinish()
                }
            }
    }
}
