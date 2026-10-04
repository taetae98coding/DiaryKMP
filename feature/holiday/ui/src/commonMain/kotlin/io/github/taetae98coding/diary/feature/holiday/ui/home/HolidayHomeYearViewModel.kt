@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.goldenHolidaySourceYearList
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HolidayHomeYearViewModel(
    @InjectedParam private val year: Int,
    private val fetchHolidayUseCase: FetchHolidayUseCase,
    private val getGoldenHolidayUseCase: GetGoldenHolidayUseCase,
) : ViewModel() {
    private val annualLeaveCount = MutableStateFlow(MIN_ANNUAL_LEAVE_COUNT)
    private val fetchState = MutableStateFlow(FetchState.NONE)
    private var isFetching = false

    val uiState: StateFlow<HolidayHomeYearUiState> =
        combine(fetchState, goldenHolidayGroupListFlow()) { fetchState, result ->
            when (fetchState) {
                FetchState.NONE, FetchState.IN_PROGRESS -> HolidayHomeYearUiState.Loading
                FetchState.FAILURE -> HolidayHomeYearUiState.Error
                FetchState.NOT_PROVIDED -> HolidayHomeYearUiState.NotProvided
                FetchState.SUCCESS -> HolidayHomeYearUiState.Content(goldenHolidayGroupList = result.getOrDefault(emptyList()))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = HolidayHomeYearUiState.Loading,
        )

    fun fetch() {
        // 화면에 다시 드러날 때마다 다시 동기화하지만, 진행 중인 동기화가 있으면 겹쳐 시작하지 않는다.
        if (isFetching) return
        isFetching = true

        if (fetchState.value != FetchState.SUCCESS) {
            fetchState.value = FetchState.IN_PROGRESS
        }

        viewModelScope.launch {
            try {
                val resultMap =
                    year
                        .goldenHolidaySourceYearList()
                        .associateWith { targetYear -> fetchHolidayUseCase(parameter = targetYear) }

                fetchState.value = fetchState(resultMap = resultMap)
            } finally {
                isFetching = false
            }
        }
    }

    fun updateAnnualLeaveCount(annualLeaveCount: Int) {
        this.annualLeaveCount.value = annualLeaveCount
    }

    private fun fetchState(resultMap: Map<Int, Result<List<Holiday>>>): FetchState =
        when {
            resultMap.values.any { result -> result.isFailure } -> FetchState.FAILURE
            resultMap.getValue(year).getOrDefault(emptyList()).isEmpty() -> FetchState.NOT_PROVIDED
            else -> FetchState.SUCCESS
        }

    private fun goldenHolidayGroupListFlow(): Flow<Result<List<GoldenHolidayGroup>>> =
        annualLeaveCount.flatMapLatest { annualLeaveCount ->
            getGoldenHolidayUseCase(
                parameter = GetGoldenHolidayUseCase.Parameter(year = year, annualLeaveCount = annualLeaveCount),
            )
        }
}

private enum class FetchState {
    NONE,
    IN_PROGRESS,
    SUCCESS,
    NOT_PROVIDED,
    FAILURE,
}
