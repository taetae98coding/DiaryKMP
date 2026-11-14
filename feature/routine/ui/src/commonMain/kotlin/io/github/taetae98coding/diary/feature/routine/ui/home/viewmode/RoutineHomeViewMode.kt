package io.github.taetae98coding.diary.feature.routine.ui.home.viewmode

internal enum class RoutineHomeViewMode {
    LIST,
    CALENDAR,
    ;

    fun toggled(): RoutineHomeViewMode =
        when (this) {
            LIST -> CALENDAR
            CALENDAR -> LIST
        }
}
