package io.github.taetae98coding.diary.data.routine.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord

internal fun CalendarRoutineRecordLocalEntity.toDomain(): CalendarRoutineRecord =
    CalendarRoutineRecord(
        routineId = routineId,
        title = title,
        color = color,
        date = date,
        amount = amount,
        unit = unit,
    )
