package io.github.taetae98coding.diary.feature.setting.ui.holiday

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountrySetting
import io.github.taetae98coding.diary.core.model.holiday.HolidaySetting

internal sealed interface SettingHolidayUiState {
    data object Loading : SettingHolidayUiState

    data class Content(
        val countrySetting: HolidayCountrySetting,
        val holidaySettingList: List<HolidaySetting>,
    ) : SettingHolidayUiState
}
