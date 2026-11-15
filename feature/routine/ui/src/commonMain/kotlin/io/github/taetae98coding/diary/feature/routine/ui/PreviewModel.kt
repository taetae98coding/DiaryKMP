package io.github.taetae98coding.diary.feature.routine.ui

import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewRoutineRecord(
    date: LocalDate = LocalDate(year = 2026, month = 7, day = 19),
    amount: Int = 250,
): RoutineRecord =
    RoutineRecord(
        id = Uuid.random(),
        routineId = Uuid.NIL,
        date = date,
        amount = amount,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewCalendarRoutineRecord(
    title: String = "물 마시기",
    date: LocalDate = LocalDate(year = 2026, month = 7, day = 19),
): CalendarRoutineRecord =
    CalendarRoutineRecord(
        routineId = Uuid.NIL,
        title = title,
        color = 0xFF3A7BD5,
        date = date,
        amount = 1500,
        unit = "ml",
    )
