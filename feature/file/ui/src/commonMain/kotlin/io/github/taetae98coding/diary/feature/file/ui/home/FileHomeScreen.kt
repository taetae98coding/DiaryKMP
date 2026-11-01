package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import kotlin.uuid.Uuid

@Composable
internal fun FileHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    componentVisibleProvider: () -> FileHomeScaffoldComponentVisible,
    fileViewModel: FileHomeViewModel,
    uploadViewModel: FileHomeUploadViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filePagingItems = fileViewModel.filePagingData.collectAsLazyPagingItems()
    val uiState by fileViewModel.uiState.collectAsStateWithLifecycle()
    val uploadUiState by uploadViewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshFailed by rememberIsRefreshFailed(filePagingItems = filePagingItems)
    var isAccountChanging by remember { mutableStateOf(false) }
    // Paging은 불러오는 중에 다시 요청하면 진행 중이던 불러오기를 버리고 처음부터 다시 시작한다.
    val refresh = {
        if (filePagingItems.loadState.refresh !is LoadState.Loading) filePagingItems.refresh()
    }

    FileHomeScreenEffect(
        uploadViewModel = uploadViewModel,
        navigateToAdd = navigateToAdd,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
        listState = listState,
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
        onAccountChangeStart = { isAccountChanging = true },
        onAccountChangeFinish = { isAccountChanging = false },
    )

    FileHomeScaffold(
        onEvent = { event ->
            when (event) {
                is FileHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is FileHomeScaffoldEvent.ClickAdd -> uploadViewModel.requestAdd()
                is FileHomeScaffoldEvent.ClickRetry -> filePagingItems.retry()
                is FileHomeScaffoldEvent.Refresh -> refresh()
                is FileHomeScaffoldEvent.ClickFile -> navigateToDetail(event.id)
            }
        },
        modifier = modifier,
        listState = listState,
        filePagingItems = filePagingItems,
        uiStateProvider = { uiState },
        uploadUiStateProvider = { uploadUiState },
        isRefreshingProvider = { filePagingItems.loadState.refresh is LoadState.Loading && filePagingItems.itemCount > 0 && !isRefreshFailed && !isAccountChanging },
        isRefreshFailedProvider = { isRefreshFailed },
        isAccountChangingProvider = { isAccountChanging },
        snackbarHostState = snackbarHostState,
        componentVisibleProvider = componentVisibleProvider,
    )
}

// Paging은 다시 불러오기에 실패해도 이전 목록을 들고 있다가 다시 시도할 때 그 목록을 다시 내보이므로,
// 실패한 뒤 새 결과가 올 때까지는 이전 목록 대신 처음 불러오기처럼 보이도록 실패를 기억한다.
@Composable
private fun rememberIsRefreshFailed(filePagingItems: LazyPagingItems<DiaryFile>): State<Boolean> {
    val isRefreshFailed = remember(filePagingItems) { mutableStateOf(false) }

    LaunchedEffect(filePagingItems) {
        snapshotFlow { filePagingItems.loadState.refresh }.collect { refresh ->
            when (refresh) {
                is LoadState.Error -> isRefreshFailed.value = true
                is LoadState.NotLoading -> isRefreshFailed.value = false
                is LoadState.Loading -> Unit
            }
        }
    }

    return isRefreshFailed
}
