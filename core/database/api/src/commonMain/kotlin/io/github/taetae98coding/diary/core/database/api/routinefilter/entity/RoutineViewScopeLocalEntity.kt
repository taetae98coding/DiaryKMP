package io.github.taetae98coding.diary.core.database.api.routinefilter.entity

public enum class RoutineViewScopeLocalEntity(
    public val persistentValue: String,
) {
    TODAY("today"),
    ALL("all"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): RoutineViewScopeLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
