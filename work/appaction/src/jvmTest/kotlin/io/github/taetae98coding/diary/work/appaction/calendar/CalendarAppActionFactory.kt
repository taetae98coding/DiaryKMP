package io.github.taetae98coding.diary.work.appaction.calendar

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun calendarAppActionOf(vararg useCaseArray: Any): CalendarAppAction {
    val useCaseList = useCaseArray.toList()

    return CalendarAppAction(
        getCalendarMemoUseCase = useCaseList.useCase(),
        getCalendarContactBirthdayUseCase = useCaseList.useCase(),
        getCalendarHolidayUseCase = useCaseList.useCase(),
    )
}
