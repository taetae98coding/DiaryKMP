package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_home_add_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebHomeScaffold(
    onEvent: (WebHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> WebHomeUiState = { WebHomeUiState() },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    componentVisibleProvider: () -> WebHomeScaffoldComponentVisible = { WebHomeScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = { WebHomeTopBar(onEvent = onEvent) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(WebHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.web_home_add_button_content_description),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryListSortBarHost(
                onClick = { onEvent(WebHomeScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
                isSortVisibleProvider = { webPagingItems.itemCount > 0 },
            )

            WebHomeList(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                webPagingItems = webPagingItems,
                isRefreshingProvider = { uiStateProvider().isRefreshing },
                sortProvider = sortProvider,
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(WebHomeScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun WebHomeScaffoldPreview() {
    DiaryTheme {
        WebHomeScaffold(onEvent = {})
    }
}
