package io.github.taetae98coding.diary.work.appaction.appfunction.calendar

import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction

internal class CalendarAppFunction(
    private val calendarAppAction: CalendarAppAction,
) {
    suspend fun memoList(
        startDate: String,
        endDate: String,
    ): List<AppFunctionCalendarMemo> = appFunctionCall { calendarAppAction.memoList(startDate = startDate, endDate = endDate).map { memo -> memo.toAppFunctionCalendarMemo() } }

    suspend fun birthdayList(
        startDate: String,
        endDate: String,
    ): List<AppFunctionCalendarBirthday> = appFunctionCall { calendarAppAction.birthdayList(startDate = startDate, endDate = endDate).map { birthday -> birthday.toAppFunctionCalendarBirthday() } }

    suspend fun holidayList(year: Int): List<AppFunctionHoliday> = appFunctionCall { calendarAppAction.holidayList(year = year).map { holiday -> holiday.toAppFunctionHoliday() } }
}
