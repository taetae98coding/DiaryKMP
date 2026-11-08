package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarBirthday
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDay
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDayLabelKindValue
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarMemo
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionHoliday
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction
import io.github.taetae98coding.diary.work.appaction.core.appActionDate
import io.github.taetae98coding.diary.work.appaction.core.appActionDateEpochMillis
import io.github.taetae98coding.diary.work.appaction.memo.toEpochDateTime

public data class DiaryAppActionCalendarMemo(
    val id: String,
    val title: String,
    val color: String,
    val dateTime: DiaryAppActionMemoDateTime,
)

public data class DiaryAppActionCalendarBirthday(
    val contactId: String,
    val name: String,
    val nameQualifier: String,
    val dateEpochMillis: Long,
)

public enum class DiaryAppActionCalendarDDayLabelKind {
    BASE_DAY,
    DAYS,
    MONTHS,
    YEARS,
}

public data class DiaryAppActionCalendarDDay(
    val ddayId: String,
    val title: String,
    val color: String,
    val dateEpochMillis: Long,
    val labelKind: DiaryAppActionCalendarDDayLabelKind,
    val labelCount: Int?,
)

public data class DiaryAppActionHoliday(
    val name: String,
    val isHoliday: Boolean,
    val startEpochMillis: Long,
    val endInclusiveEpochMillis: Long,
)

public object DiaryCalendarAppAction {
    public suspend fun memoList(
        startDateEpochMillis: Long,
        endDateEpochMillis: Long,
    ): List<DiaryAppActionCalendarMemo> =
        calendarAppAction()
            .memoList(startDate = appActionDate(epochMillis = startDateEpochMillis), endDate = appActionDate(epochMillis = endDateEpochMillis))
            .map { memo -> memo.toDiary() }

    public suspend fun birthdayList(
        startDateEpochMillis: Long,
        endDateEpochMillis: Long,
    ): List<DiaryAppActionCalendarBirthday> =
        calendarAppAction()
            .birthdayList(startDate = appActionDate(epochMillis = startDateEpochMillis), endDate = appActionDate(epochMillis = endDateEpochMillis))
            .map { birthday -> birthday.toDiary() }

    public suspend fun ddayList(
        startDateEpochMillis: Long,
        endDateEpochMillis: Long,
    ): List<DiaryAppActionCalendarDDay> =
        calendarAppAction()
            .ddayList(startDate = appActionDate(epochMillis = startDateEpochMillis), endDate = appActionDate(epochMillis = endDateEpochMillis))
            .map { dday -> dday.toDiary() }

    public suspend fun holidayList(year: Int): List<DiaryAppActionHoliday> = calendarAppAction().holidayList(year = year).map { holiday -> holiday.toDiary() }
}

private fun calendarAppAction(): CalendarAppAction = diaryAppAction()

private fun AppActionCalendarMemo.toDiary(): DiaryAppActionCalendarMemo =
    DiaryAppActionCalendarMemo(
        id = id,
        title = title,
        color = color,
        dateTime = dateTime.toEpochDateTime().toDiary(),
    )

private fun AppActionCalendarBirthday.toDiary(): DiaryAppActionCalendarBirthday =
    DiaryAppActionCalendarBirthday(
        contactId = contactId,
        name = name,
        nameQualifier = nameQualifier,
        dateEpochMillis = appActionDateEpochMillis(date = date),
    )

private fun AppActionCalendarDDay.toDiary(): DiaryAppActionCalendarDDay =
    DiaryAppActionCalendarDDay(
        ddayId = ddayId,
        title = title,
        color = color,
        dateEpochMillis = appActionDateEpochMillis(date = date),
        labelKind = label.kind.toDiaryLabelKind(),
        labelCount = label.count,
    )

private fun String.toDiaryLabelKind(): DiaryAppActionCalendarDDayLabelKind =
    when (this) {
        AppActionCalendarDDayLabelKindValue.BASE_DAY -> DiaryAppActionCalendarDDayLabelKind.BASE_DAY
        AppActionCalendarDDayLabelKindValue.DAYS -> DiaryAppActionCalendarDDayLabelKind.DAYS
        AppActionCalendarDDayLabelKindValue.MONTHS -> DiaryAppActionCalendarDDayLabelKind.MONTHS
        AppActionCalendarDDayLabelKindValue.YEARS -> DiaryAppActionCalendarDDayLabelKind.YEARS
        else -> error("Unknown calendar D-Day label kind: $this")
    }

private fun AppActionHoliday.toDiary(): DiaryAppActionHoliday =
    DiaryAppActionHoliday(
        name = name,
        isHoliday = isHoliday,
        startEpochMillis = appActionDateEpochMillis(date = start),
        endInclusiveEpochMillis = appActionDateEpochMillis(date = endInclusive),
    )
