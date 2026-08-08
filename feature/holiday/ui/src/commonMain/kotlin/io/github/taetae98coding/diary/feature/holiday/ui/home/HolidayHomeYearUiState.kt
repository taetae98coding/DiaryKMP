package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.Immutable
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup

@Immutable
internal sealed interface HolidayHomeYearUiState {
    data object Loading : HolidayHomeYearUiState

    data object Error : HolidayHomeYearUiState

    data object NotProvided : HolidayHomeYearUiState

    data class Loaded(
        val goldenHolidayGroupList: List<GoldenHolidayGroup> = emptyList(),
    ) : HolidayHomeYearUiState
}
