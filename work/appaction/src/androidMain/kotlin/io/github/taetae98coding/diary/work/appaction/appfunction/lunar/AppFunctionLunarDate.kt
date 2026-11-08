package io.github.taetae98coding.diary.work.appaction.appfunction.lunar

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.lunar.AppActionLunarDate

/** The lunar date of a solar date. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionLunarDate(
    /** Solar date, like 2026-01-31. */
    val solarDate: String,
    /** Lunar year. */
    val lunarYear: Int,
    /** Lunar month. */
    val lunarMonth: Int,
    /** Lunar day. */
    val lunarDay: Int,
    /** Whether the lunar month is a leap month. */
    val isLeapMonth: Boolean,
)

internal fun AppActionLunarDate.toAppFunctionLunarDate(): AppFunctionLunarDate =
    AppFunctionLunarDate(
        solarDate = solarDate,
        lunarYear = lunarYear,
        lunarMonth = lunarMonth,
        lunarDay = lunarDay,
        isLeapMonth = isLeapMonth,
    )
