package io.github.taetae98coding.diary.feature.setting.ui.holiday

import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption

internal sealed interface SettingHolidayScaffoldEvent {
    data object ClickNavigateUp : SettingHolidayScaffoldEvent

    data object ClickSelectAll : SettingHolidayScaffoldEvent

    data object ClickDeselectAll : SettingHolidayScaffoldEvent

    data object ClickSelectDaysOff : SettingHolidayScaffoldEvent

    data class ToggleHoliday(
        val name: String,
    ) : SettingHolidayScaffoldEvent

    data class ToggleCountryOption(
        val option: HolidayCountryOption,
    ) : SettingHolidayScaffoldEvent
}
