package io.github.taetae98coding.diary.feature.holiday.ui.home

internal sealed interface HolidayHomeScaffoldEvent {
    data object ClickNavigateUp : HolidayHomeScaffoldEvent
}
