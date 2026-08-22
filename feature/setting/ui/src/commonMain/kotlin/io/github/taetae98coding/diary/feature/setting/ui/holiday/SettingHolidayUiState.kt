package io.github.taetae98coding.diary.feature.setting.ui.holiday

import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting

internal sealed interface SettingHolidayUiState {
    data object Loading : SettingHolidayUiState

    data class Loaded(
        val holidaySettingList: List<HolidaySetting>,
    ) : SettingHolidayUiState
}
