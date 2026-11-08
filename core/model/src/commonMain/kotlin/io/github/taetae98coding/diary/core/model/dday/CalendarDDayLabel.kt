package io.github.taetae98coding.diary.core.model.dday

public sealed interface CalendarDDayLabel {
    public data object BaseDay : CalendarDDayLabel

    public data class Years(
        val years: Int,
    ) : CalendarDDayLabel

    public data class Months(
        val months: Int,
    ) : CalendarDDayLabel

    public data class Days(
        val days: Int,
    ) : CalendarDDayLabel
}
