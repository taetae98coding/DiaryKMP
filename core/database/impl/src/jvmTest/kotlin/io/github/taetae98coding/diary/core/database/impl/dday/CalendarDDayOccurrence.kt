package io.github.taetae98coding.diary.core.database.impl.dday

import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import kotlinx.datetime.LocalDate

// 캘린더 디데이 조회 결과에서 날짜와 문구를 정하는 값만 비교하도록 줄여 쓴다. 문구 `100일`은 일 100, `6개월`은 개월 6, `1주년`은 주년 1이다.
internal data class CalendarDDayOccurrence(
    val date: LocalDate,
    val kind: CalendarDDayKindLocalEntity,
    val amount: Int,
)

internal fun baseDay(date: LocalDate): CalendarDDayOccurrence = CalendarDDayOccurrence(date = date, kind = CalendarDDayKindLocalEntity.BASE_DAY, amount = 0)

internal fun days(
    date: LocalDate,
    days: Int,
): CalendarDDayOccurrence = CalendarDDayOccurrence(date = date, kind = CalendarDDayKindLocalEntity.DAYS, amount = days)

internal fun months(
    date: LocalDate,
    months: Int,
): CalendarDDayOccurrence = CalendarDDayOccurrence(date = date, kind = CalendarDDayKindLocalEntity.MONTHS, amount = months)

internal fun years(
    date: LocalDate,
    years: Int,
): CalendarDDayOccurrence = CalendarDDayOccurrence(date = date, kind = CalendarDDayKindLocalEntity.YEARS, amount = years)

internal fun CalendarDDayLocalEntity.toOccurrence(): CalendarDDayOccurrence = CalendarDDayOccurrence(date = date, kind = kind, amount = amount)
