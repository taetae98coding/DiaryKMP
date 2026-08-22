package io.github.taetae98coding.diary.feature.setting.ui.holiday

internal sealed interface SettingHolidayScaffoldEvent {
    data object ClickNavigateUp : SettingHolidayScaffoldEvent

    data object ClickSelectAll : SettingHolidayScaffoldEvent

    data object ClickDeselectAll : SettingHolidayScaffoldEvent

    data object ClickSelectDaysOff : SettingHolidayScaffoldEvent

    data class ToggleHoliday(
        val key: String,
    ) : SettingHolidayScaffoldEvent
}
