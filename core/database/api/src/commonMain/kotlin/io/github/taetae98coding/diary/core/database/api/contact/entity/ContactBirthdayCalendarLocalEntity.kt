package io.github.taetae98coding.diary.core.database.api.contact.entity

public enum class ContactBirthdayCalendarLocalEntity(
    public val persistentValue: String,
) {
    SOLAR("solar"),
    LUNAR("lunar"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): ContactBirthdayCalendarLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
