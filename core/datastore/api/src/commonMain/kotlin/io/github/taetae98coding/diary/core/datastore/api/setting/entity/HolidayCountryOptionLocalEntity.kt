package io.github.taetae98coding.diary.core.datastore.api.setting.entity

public enum class HolidayCountryOptionLocalEntity(
    public val persistentValue: String,
) {
    DEVICE("device"),
    KOREA("kr"),
    UNITED_STATES("us"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): HolidayCountryOptionLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
