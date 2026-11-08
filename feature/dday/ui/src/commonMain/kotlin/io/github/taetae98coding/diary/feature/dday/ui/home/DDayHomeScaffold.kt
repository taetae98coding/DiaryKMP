package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.addShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.previewDDay
import io.github.taetae98coding.diary.compose.dday.previewDDayToday
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_add_button_content_description
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayHomeScaffold(
    onEvent: (DDayHomeScaffoldEvent) -> Unit,
    onDDayListEvent: (DDayListEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: DDayListState = rememberDDayListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    ddayPagingItems: LazyPagingItems<DDay> = remember { flowOf(PagingData.empty<DDay>()) }.collectAsLazyPagingItems(),
    filterUiStateProvider: () -> DDayHomeScaffoldFilterUiState = { DDayHomeScaffoldFilterUiState() },
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    componentVisibleProvider: () -> DDayHomeScaffoldComponentVisible = { DDayHomeScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier.addShortcut(isEnabledProvider = { componentVisibleProvider().isAddButtonVisible }) { onEvent(DDayHomeScaffoldEvent.ClickAdd) },
        topBar = {
            DDayHomeTopBar(
                onEvent = onEvent,
                filterUiStateProvider = filterUiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(DDayHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.dday_home_add_button_content_description),
                )
            }
        },
    ) { paddingValues ->
        DDayHomeList(
            onEvent = onEvent,
            onDDayListEvent = onDDayListEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            listState = listState,
            gridState = gridState,
            ddayPagingItems = ddayPagingItems,
            isRefreshingProvider = { syncUiStateProvider().isRefreshing },
            filterUiStateProvider = filterUiStateProvider,
            bottomPadding = if (componentVisibleProvider().isAddButtonVisible) DiaryTheme.dimens.floatingActionButtonClearance else DiaryTheme.dimens.screenVerticalPadding,
        )
    }
}

@ScreenPreview
@Composable
private fun DDayHomeScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasDDay: Boolean,
) {
    val pagingData =
        remember(hasDDay) {
            flowOf(if (hasDDay) PagingData.from(listOf(previewDDay(title = "만난 날"), previewDDay(title = "시험"))) else PagingData.empty())
        }

    DiaryTheme {
        DDayHomeScaffold(
            onEvent = {},
            onDDayListEvent = {},
            listState = rememberDDayListState(initialToday = previewDDayToday()),
            ddayPagingItems = pagingData.collectAsLazyPagingItems(),
        )
    }
}
