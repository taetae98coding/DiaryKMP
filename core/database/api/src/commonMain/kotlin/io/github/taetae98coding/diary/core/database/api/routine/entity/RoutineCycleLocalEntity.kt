package io.github.taetae98coding.diary.core.database.api.routine.entity

public enum class RoutineCycleLocalEntity(
    public val persistentValue: String,
) {
    DAILY("FREQ=DAILY"),
    WEEKDAYS("FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR"),
    WEEKENDS("FREQ=DAILY;BYDAY=SA,SU"),
    WEEKLY("FREQ=WEEKLY;WKST=SU"),
    MONTHLY("FREQ=MONTHLY"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): RoutineCycleLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
