package io.github.taetae98coding.diary.data.dday.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel

internal fun CalendarDDayLocalEntity.toDomain(): CalendarDDay =
    CalendarDDay(
        ddayId = ddayId,
        title = title,
        color = color,
        date = date,
        label =
            when (kind) {
                CalendarDDayKindLocalEntity.BASE_DAY -> CalendarDDayLabel.BaseDay
                CalendarDDayKindLocalEntity.YEARS -> CalendarDDayLabel.Years(years = amount)
                CalendarDDayKindLocalEntity.MONTHS -> CalendarDDayLabel.Months(months = amount)
                CalendarDDayKindLocalEntity.DAYS -> CalendarDDayLabel.Days(days = amount)
            },
    )
