package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.button.ListEntryButton
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
import io.github.taetae98coding.diary.compose.core.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGridDefaults
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PullToRefreshGestureBox
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.compose.routine.previewRoutineDetail
import io.github.taetae98coding.diary.compose.routine.previewRoutineSummary
import io.github.taetae98coding.diary.compose.routine.previewRoutineToday
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.home.amount.RoutineAmountDialogState
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineQuickRecordButton
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_empty_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_empty_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filtered_empty_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filtered_empty_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_finished_list_action_label
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_today_empty_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_today_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun RoutineHomeList(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    filterUiStateProvider: () -> RoutineHomeScaffoldFilterUiState = { RoutineHomeScaffoldFilterUiState() },
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    ListQueryScrollEffect(
        gridState = state.gridState,
        filterProvider = { filterUiStateProvider().listQueryFilter },
        itemListProvider = { routinePagingItems.itemSnapshotList.items.map { summary -> summary.routine.id } },
        isRefreshingProvider = { routinePagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryPullToRefreshBox(
        onRefresh = { onEvent(RoutineHomeScaffoldEvent.Refresh) },
        modifier = modifier,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        // 완료된 루틴 진입 줄과 빈 상태처럼 스크롤할 내용이 없는 자리에서 당겨도 새로고침이 시작되게 한다.
        PullToRefreshGestureBox(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                RoutineHomeFinishedListBar(
                    onClick = { onEvent(RoutineHomeScaffoldEvent.ClickFinishedList) },
                    modifier = Modifier.fillMaxWidth(),
                )
                DiaryCrossfade(
                    targetState = routinePagingItems.isLoadedEmpty(),
                    modifier = Modifier.weight(1F),
                ) { isEmpty ->
                    if (isEmpty) {
                        RoutineHomeEmpty(
                            modifier = Modifier.fillMaxSize(),
                            emptyReasonProvider = { filterUiStateProvider().emptyReason },
                        )
                    } else {
                        RoutineHomeGrid(
                            onEvent = onEvent,
                            onRoutineListEvent = onRoutineListEvent,
                            state = state,
                            routinePagingItems = routinePagingItems,
                            modifier = Modifier.fillMaxSize(),
                            bottomPadding = bottomPadding,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineHomeFinishedListBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiaryListSortBarHost(
        onClick = {},
        modifier = modifier,
        isSortVisibleProvider = { false },
        trailing = {
            ListEntryButton(
                onClick = onClick,
                label = stringResource(Res.string.routine_home_finished_list_action_label),
            )
        },
    )
}

@Composable
private fun RoutineHomeGrid(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    state: RoutineHomeScaffoldState,
    routinePagingItems: LazyPagingItems<RoutineSummary>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    val gridState = state.gridState

    LazyVerticalGrid(
        columns = GridCells.Fixed(DiaryRefreshableGridDefaults.COLUMN_COUNT),
        modifier = modifier.systemScroll(gridState) { gridState.animateScrollToItem(0) },
        state = gridState,
        contentPadding = DiaryRefreshableGridDefaults.contentPadding(bottomPadding = bottomPadding),
        horizontalArrangement = DiaryRefreshableGridDefaults.ItemArrangement,
        verticalArrangement = DiaryRefreshableGridDefaults.ItemArrangement,
    ) {
        items(
            count = routinePagingItems.itemCount,
            key = routinePagingItems.itemKey { summary -> summary.routine.id },
            span = { GridItemSpan(maxLineSpan) },
        ) { index ->
            SwipeRoutineCard(
                onEvent = onRoutineListEvent,
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                state = state.listState,
                summary = routinePagingItems[index],
                quickRecord = { summary ->
                    RoutineHomeQuickRecordButton(
                        onEvent = onEvent,
                        summary = summary,
                        state = state,
                    )
                },
            )
        }
    }
}

@Composable
private fun RoutineHomeQuickRecordButton(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    summary: RoutineSummary,
    state: RoutineHomeScaffoldState,
    modifier: Modifier = Modifier,
) {
    val detail = summary.routine.detail
    val focusRequester = remember { FocusRequester() }

    RoutineAmountDialogFocusReturnEffect(
        routineId = summary.routine.id,
        amountDialogState = state.amountDialogState,
        focusRequester = focusRequester,
    )

    RoutineQuickRecordButton(
        onClick = {
            onEvent(
                RoutineHomeScaffoldEvent.QuickRecord(
                    record = RoutineHomeRecord(routineId = summary.routine.id, title = detail.title, unit = detail.unit, amount = detail.amountPerRecord),
                ),
            )
        },
        onAmountInputRequest = { state.amountDialogState.show(routine = summary.routine) },
        title = detail.title,
        amount = detail.amountPerRecord,
        unit = detail.unit,
        modifier = modifier.focusRequester(focusRequester),
    )
}

@Composable
private fun RoutineAmountDialogFocusReturnEffect(
    routineId: Uuid,
    amountDialogState: RoutineAmountDialogState,
    focusRequester: FocusRequester,
) {
    LaunchedEffect(routineId, amountDialogState, focusRequester) {
        var wasOpened = false

        snapshotFlow { amountDialogState.target?.routineId == routineId }.collect { isOpened ->
            if (wasOpened && !isOpened) {
                // Scaffold의 keyShortcut이 창이 닫히는 순간 화면 루트로 초점을 가져가므로, 그 뒤 프레임에 창을 연 버튼으로 초점을 돌려준다.
                withFrameNanos { }
                focusRequester.requestFocus()
            }
            wasOpened = isOpened
        }
    }
}

@Composable
private fun RoutineHomeEmpty(
    modifier: Modifier = Modifier,
    emptyReasonProvider: () -> RoutineHomeEmptyReason = { RoutineHomeEmptyReason.NO_ROUTINE },
) {
    DiaryCrossfade(
        targetState = emptyReasonProvider(),
        modifier = modifier,
    ) { emptyReason ->
        when (emptyReason) {
            RoutineHomeEmptyReason.NO_ROUTINE ->
                DiaryEmptyBox(
                    title = stringResource(Res.string.routine_home_empty_title),
                    description = stringResource(Res.string.routine_home_empty_description),
                    icon = { RoutineIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )

            RoutineHomeEmptyReason.NO_TODAY_ROUTINE ->
                DiaryEmptyBox(
                    title = stringResource(Res.string.routine_home_today_empty_title),
                    description = stringResource(Res.string.routine_home_today_empty_description),
                    icon = { RoutineIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )

            RoutineHomeEmptyReason.NO_FILTERED_ROUTINE ->
                DiaryEmptyBox(
                    title = stringResource(Res.string.routine_home_filtered_empty_title),
                    description = stringResource(Res.string.routine_home_filtered_empty_description),
                    icon = { FilterIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
        }
    }
}

@ScreenPreview
@Composable
private fun RoutineHomeListPreview() {
    val summaryList =
        remember {
            listOf(
                previewRoutineSummary(title = "물 마시기"),
                previewRoutineSummary(title = "달리기", cycleAmount = 12_000, detail = previewRoutineDetail(title = "달리기", goalAmount = null, color = 0xFFE57373)),
            )
        }
    val pagingData = remember(summaryList) { flowOf(PagingData.from(summaryList)) }

    DiaryTheme {
        RoutineHomeList(
            onEvent = {},
            onRoutineListEvent = {},
            modifier = Modifier.fillMaxSize(),
            state = rememberRoutineHomeScaffoldState(listState = rememberRoutineListState(initialToday = previewRoutineToday())),
            routinePagingItems = pagingData.collectAsLazyPagingItems(),
        )
    }
}

private class RoutineHomeEmptyReasonPreviewParameter : PreviewParameterProvider<RoutineHomeEmptyReason> {
    override val values: Sequence<RoutineHomeEmptyReason> = RoutineHomeEmptyReason.entries.asSequence()
}

@ScreenPreview
@Composable
private fun RoutineHomeEmptyPreview(
    @PreviewParameter(RoutineHomeEmptyReasonPreviewParameter::class) emptyReason: RoutineHomeEmptyReason,
) {
    DiaryTheme {
        RoutineHomeEmpty(
            modifier = Modifier.fillMaxSize(),
            emptyReasonProvider = { emptyReason },
        )
    }
}
