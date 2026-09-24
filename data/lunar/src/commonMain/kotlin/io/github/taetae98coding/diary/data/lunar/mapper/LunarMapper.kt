package io.github.taetae98coding.diary.data.lunar.mapper

import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.core.model.lunar.LunarDate

internal fun LunarDateRemoteEntity.toLocal(solarYear: Int): LunarDateLocalEntity =
    LunarDateLocalEntity(
        solar = solar,
        solarYear = solarYear,
        lunarYear = year,
        lunarMonth = month,
        lunarDay = day,
        isLeapMonth = isLeapMonth,
    )

internal fun LunarDateLocalEntity.toDomain(): LunarDate =
    LunarDate(
        solar = solar,
        year = lunarYear,
        month = lunarMonth,
        day = lunarDay,
        isLeapMonth = isLeapMonth,
    )
