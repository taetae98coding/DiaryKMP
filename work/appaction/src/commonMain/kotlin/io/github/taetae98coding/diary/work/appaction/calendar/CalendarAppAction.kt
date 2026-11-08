package io.github.taetae98coding.diary.work.appaction.calendar

import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.work.appaction.core.dateRangeArgument
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import org.koin.core.annotation.Factory

@Factory
public class CalendarAppAction internal constructor(
    private val getCalendarMemoUseCase: GetCalendarMemoUseCase,
    private val getCalendarContactBirthdayUseCase: GetCalendarContactBirthdayUseCase,
    private val getCalendarHolidayUseCase: GetCalendarHolidayUseCase,
    private val getCalendarDDayUseCase: GetCalendarDDayUseCase,
) {
    public suspend fun memoList(
        startDate: String,
        endDate: String,
    ): List<AppActionCalendarMemo> =
        runAppAction {
            getCalendarMemoUseCase
                .readFirst(parameter = dateRangeArgument(startDate = startDate, endDate = endDate))
                .map { memo -> memo.toAppActionCalendarMemo() }
        }

    public suspend fun birthdayList(
        startDate: String,
        endDate: String,
    ): List<AppActionCalendarBirthday> =
        runAppAction {
            getCalendarContactBirthdayUseCase
                .readFirst(parameter = dateRangeArgument(startDate = startDate, endDate = endDate))
                .map { birthday -> birthday.toAppActionCalendarBirthday() }
        }

    public suspend fun ddayList(
        startDate: String,
        endDate: String,
    ): List<AppActionCalendarDDay> =
        runAppAction {
            getCalendarDDayUseCase
                .readFirst(parameter = dateRangeArgument(startDate = startDate, endDate = endDate))
                .sortedBy { dday -> dday.date }
                .map { dday -> dday.toAppActionCalendarDDay() }
        }

    public suspend fun holidayList(year: Int): List<AppActionHoliday> =
        runAppAction {
            getCalendarHolidayUseCase
                .readFirst(parameter = year)
                .map { holiday -> holiday.toAppActionHoliday() }
        }
}
