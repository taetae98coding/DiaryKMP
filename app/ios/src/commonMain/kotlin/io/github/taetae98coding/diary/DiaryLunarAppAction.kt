package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionDate
import io.github.taetae98coding.diary.work.appaction.core.appActionDateEpochMillis
import io.github.taetae98coding.diary.work.appaction.lunar.AppActionLunarDate
import io.github.taetae98coding.diary.work.appaction.lunar.LunarAppAction

public data class DiaryAppActionLunarDate(
    val solarDateEpochMillis: Long,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean,
)

public object DiaryLunarAppAction {
    public suspend fun list(
        startDateEpochMillis: Long,
        endDateEpochMillis: Long,
    ): List<DiaryAppActionLunarDate> =
        lunarAppAction()
            .list(startDate = appActionDate(epochMillis = startDateEpochMillis), endDate = appActionDate(epochMillis = endDateEpochMillis))
            .map { lunarDate -> lunarDate.toDiary() }
}

private fun lunarAppAction(): LunarAppAction = diaryAppAction()

private fun AppActionLunarDate.toDiary(): DiaryAppActionLunarDate =
    DiaryAppActionLunarDate(
        solarDateEpochMillis = appActionDateEpochMillis(date = solarDate),
        lunarYear = lunarYear,
        lunarMonth = lunarMonth,
        lunarDay = lunarDay,
        isLeapMonth = isLeapMonth,
    )
