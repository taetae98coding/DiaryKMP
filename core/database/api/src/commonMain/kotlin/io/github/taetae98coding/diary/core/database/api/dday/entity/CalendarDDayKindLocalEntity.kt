package io.github.taetae98coding.diary.core.database.api.dday.entity

public enum class CalendarDDayKindLocalEntity(
    public val queryValue: String,
) {
    BASE_DAY("base_day"),
    YEARS("years"),
    MONTHS("months"),
    DAYS("days"),
    ;

    public companion object {
        public fun fromQueryValue(queryValue: String): CalendarDDayKindLocalEntity? = entries.find { entry -> entry.queryValue == queryValue }
    }
}
