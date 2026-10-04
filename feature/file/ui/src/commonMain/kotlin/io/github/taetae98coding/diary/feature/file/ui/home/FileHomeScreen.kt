package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
internal fun FileHomeScreen(
    navigateUp: () -> Unit,
    navigateToAdd: () -> Unit,
    fileViewModel: FileHomeViewModel,
    uploadViewModel: FileHomeUploadViewModel,
    refreshViewModel: FileHomeRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filePagingItems = fileViewModel.filePagingData.collectAsLazyPagingItems()
    val uiState by fileViewModel.uiState.collectAsStateWithLifecycle()
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

    FileHomeScreenEffect(
        uploadViewModel = uploadViewModel,
        refreshViewModel = refreshViewModel,
        fileViewModel = fileViewModel,
        navigateToAdd = navigateToAdd,
        uiStateProvider = { uiState },
        refreshUiStateProvider = { refreshUiState },
        listState = listState,
        filePagingItems = filePagingItems,
        snackbarHostState = snackbarHostState,
        refresh = refresh,
    )

    FileHomeScaffold(
        onEvent = { event ->
            when (event) {
                is FileHomeScaffoldEvent.ClickNavigateUp -> navigateUp()
                is FileHomeScaffoldEvent.ClickAdd -> uploadViewModel.requestAdd()
                is FileHomeScaffoldEvent.ClickRetry -> filePagingItems.retry()
                is FileHomeScaffoldEvent.Refresh -> refresh()
            }
        },
        modifier = modifier,
        listState = listState,
        filePagingItems = filePagingItems,
        uiStateProvider = { uiState },
        uploadUiStateProvider = { uploadUiState },
        isRefreshingProvider = { refreshUiState.isRefreshing },
        snackbarHostState = snackbarHostState,
    )
}
