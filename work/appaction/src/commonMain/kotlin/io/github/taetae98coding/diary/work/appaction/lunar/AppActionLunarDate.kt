package io.github.taetae98coding.diary.work.appaction.lunar

import io.github.taetae98coding.diary.core.model.lunar.LunarDate

public data class AppActionLunarDate(
    val solarDate: String,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean,
)

internal fun LunarDate.toAppActionLunarDate(): AppActionLunarDate =
    AppActionLunarDate(
        solarDate = solar.toString(),
        lunarYear = year,
        lunarMonth = month,
        lunarDay = day,
        isLeapMonth = isLeapMonth,
    )
