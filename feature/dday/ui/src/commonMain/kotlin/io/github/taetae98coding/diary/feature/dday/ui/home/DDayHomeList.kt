package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.DDayIcon
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.SwipeDDayCard
import io.github.taetae98coding.diary.compose.dday.previewDDay
import io.github.taetae98coding.diary.compose.dday.previewDDayToday
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_empty_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_empty_title
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_filtered_empty_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_filtered_empty_title
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayHomeList(
    onEvent: (DDayHomeScaffoldEvent) -> Unit,
    onDDayListEvent: (DDayListEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: DDayListState = rememberDDayListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    ddayPagingItems: LazyPagingItems<DDay> = remember { flowOf(PagingData.empty<DDay>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    filterUiStateProvider: () -> DDayHomeScaffoldFilterUiState = { DDayHomeScaffoldFilterUiState() },
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    ListQueryScrollEffect(
        gridState = gridState,
        filterProvider = { filterUiStateProvider().listQueryFilter },
        itemListProvider = { ddayPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { ddayPagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryCrossfade(
        targetState = ddayPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(DDayHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DDayHomeEmpty(
                    // 당김 새로고침은 중첩 스크롤로만 시작되므로 스크롤할 내용이 없어도 verticalScroll을 둔다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    isFilterAppliedProvider = { filterUiStateProvider().isApplied },
                )
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = { onEvent(DDayHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                bottomPadding = bottomPadding,
            ) {
                items(
                    count = ddayPagingItems.itemCount,
                    key = ddayPagingItems.itemKey { dday -> dday.id },
                ) { index ->
                    SwipeDDayCard(
                        onEvent = onDDayListEvent,
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        state = listState,
                        dday = ddayPagingItems[index],
                    )
                }
            }
        }
    }
}

@Composable
private fun DDayHomeEmpty(
    modifier: Modifier = Modifier,
    isFilterAppliedProvider: () -> Boolean = { false },
) {
    DiaryCrossfade(
        targetState = isFilterAppliedProvider(),
        modifier = modifier,
    ) { isFilterApplied ->
        if (isFilterApplied) {
            DiaryEmptyBox(
                title = stringResource(Res.string.dday_home_filtered_empty_title),
                modifier = Modifier.fillMaxSize(),
                description = stringResource(Res.string.dday_home_filtered_empty_description),
                icon = { FilterIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        } else {
            DiaryEmptyBox(
                title = stringResource(Res.string.dday_home_empty_title),
                modifier = Modifier.fillMaxSize(),
                description = stringResource(Res.string.dday_home_empty_description),
                icon = { DDayIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

@ScreenPreview
@Composable
private fun DDayHomeListPreview() {
    val ddayList =
        remember {
            listOf(
                previewDDay(title = "만난 날"),
                previewDDay(title = "시험", baseDate = LocalDate(year = 2026, month = 4, day = 29), color = 0xFFE57373),
            )
        }
    val pagingData = remember(ddayList) { flowOf(PagingData.from(ddayList)) }

    DiaryTheme {
        DDayHomeList(
            onEvent = {},
            onDDayListEvent = {},
            modifier = Modifier.fillMaxSize(),
            listState = rememberDDayListState(initialToday = previewDDayToday()),
            ddayPagingItems = pagingData.collectAsLazyPagingItems(),
        )
    }
}

@ScreenPreview
@Composable
private fun DDayHomeEmptyPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFilterApplied: Boolean,
) {
    DiaryTheme {
        DDayHomeEmpty(
            modifier = Modifier.fillMaxSize(),
            isFilterAppliedProvider = { isFilterApplied },
        )
    }
}
