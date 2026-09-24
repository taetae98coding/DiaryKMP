package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import kotlinx.datetime.number

internal fun List<LunarContactBirthday>.toCalendarContactBirthdayList(lunarDateList: List<LunarDate>): List<CalendarContactBirthday> {
    val lunarDateListByMonthDay =
        lunarDateList
            .filterNot { lunarDate -> lunarDate.isLeapMonth }
            .groupBy { lunarDate -> lunarDate.month to lunarDate.day }

    return flatMap { birthday ->
        lunarDateListByMonthDay[birthday.birthday.month.number to birthday.birthday.day]
            .orEmpty()
            .filter { lunarDate -> lunarDate.year >= birthday.birthday.year }
            .map { lunarDate ->
                CalendarContactBirthday(
                    contactId = birthday.contactId,
                    name = birthday.name,
                    date = lunarDate.solar,
                )
            }
    }
}

internal fun List<CalendarContactBirthday>.sortedForCalendar(): List<CalendarContactBirthday> =
    sortedWith(
        compareBy<CalendarContactBirthday> { birthday -> birthday.date }
            .thenBy { birthday -> birthday.name }
            .thenBy { birthday -> birthday.contactId.toString() },
    )
