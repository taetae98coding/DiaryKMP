package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineViewScopeUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetUnfinishedRoutineExistsUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddUiState
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * 태그 입력은 TagAdd 결과를 [LocalResultEventBus]로 받으므로, 루틴 입력 화면을 직접 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun RoutineTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}

internal fun loadedRoutinePagingData(summaryList: List<RoutineSummary> = emptyList()): PagingData<RoutineSummary> =
    PagingData.from(
        data = summaryList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

// 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
internal fun routineHomeViewModel(): RoutineHomeViewModel =
    mockk<RoutineHomeViewModel>(relaxed = true).apply {
        every { routinePagingData } returns MutableStateFlow(loadedRoutinePagingData())
        every { filterUiState } returns MutableStateFlow(RoutineHomeScaffoldFilterUiState())
        every { effect } returns emptyFlow()
        every { recordEffect } returns emptyFlow()
    }

internal fun routineHomeCalendarViewModel(): RoutineHomeCalendarViewModel =
    mockk<RoutineHomeCalendarViewModel>(relaxed = true).apply {
        every { uiState } returns MutableStateFlow(RoutineHomeCalendarUiState())
    }

internal fun routineHomeHolidayViewModel(): RoutineHomeHolidayViewModel =
    mockk<RoutineHomeHolidayViewModel>(relaxed = true).apply {
        every { uiState } returns MutableStateFlow(RoutineHomeHolidayUiState())
    }

internal fun routineSyncRefreshViewModel(): SyncRefreshViewModel =
    mockk<SyncRefreshViewModel>(relaxed = true).apply {
        every { uiState } returns MutableStateFlow(SyncRefreshUiState())
    }

internal fun routineAddViewModel(): RoutineAddViewModel =
    mockk<RoutineAddViewModel>(relaxed = true).apply {
        every { uiState } returns MutableStateFlow(RoutineAddUiState())
        every { effect } returns emptyFlow()
    }

internal fun routineAddTagViewModel(): EntityAddTagViewModel =
    mockk<EntityAddTagViewModel>(relaxed = true).apply {
        every { uiState } returns MutableStateFlow(EntityTagInputUiState())
        every { selectionUiState } returns MutableStateFlow(EntityAddTagSelectionUiState(tagIdSet = emptySet()))
        every { tagPagingData } returns MutableStateFlow(PagingData.empty())
        every { selectableTagPagingData } returns MutableStateFlow(PagingData.empty())
    }

internal fun routineViewScopeUseCase(viewScope: Flow<RoutineViewScope> = flowOf(ROUTINE_DEFAULT_VIEW_SCOPE)): GetRoutineViewScopeUseCase {
    val useCase = mockk<GetRoutineViewScopeUseCase>()
    every { useCase() } returns viewScope.map { value -> Result.success(value) }

    return useCase
}

internal fun unfinishedRoutineExistsUseCase(exists: Flow<Boolean> = flowOf(false)): GetUnfinishedRoutineExistsUseCase {
    val useCase = mockk<GetUnfinishedRoutineExistsUseCase>()
    every { useCase() } returns exists.map { value -> Result.success(value) }

    return useCase
}
