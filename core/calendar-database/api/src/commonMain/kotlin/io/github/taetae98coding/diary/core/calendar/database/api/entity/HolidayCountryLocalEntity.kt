package io.github.taetae98coding.diary.core.calendar.database.api.entity

public enum class HolidayCountryLocalEntity(
    public val persistentValue: String,
) {
    KOREA("kr"),
    UNITED_STATES("us"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): HolidayCountryLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
