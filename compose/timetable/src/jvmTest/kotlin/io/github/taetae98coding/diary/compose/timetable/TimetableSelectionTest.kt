package io.github.taetae98coding.diary.compose.timetable

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class TimetableSelectionTest :
    FunSpec({
        test("TC-TIMETABLE-DOMAIN-015 시간대 선택의 칸은 30분 단위이고 하루의 마지막 칸은 23시 59분에 끝난다") {
            val caseList =
                listOf(
                    (0 to 0) to ((0 to 0) to (0 to 30)),
                    (10 to 10) to ((10 to 0) to (10 to 30)),
                    (10 to 30) to ((10 to 30) to (11 to 0)),
                    (23 to 45) to ((23 to 30) to (23 to 59)),
                )

            caseList.forEach { (position, expected) ->
                val slot = timetableTimeSlotAt(date = DATE, minuteOfDay = position.first * 60 + position.second)

                TimetableSelection.Time(anchor = slot, current = slot).toEvent() shouldBe
                    TimetableEvent.SelectTime(
                        start = dateTime(date = DATE, time = expected.first),
                        endInclusive = dateTime(date = DATE, time = expected.second),
                    )
            }
        }

        test("TC-TIMETABLE-DOMAIN-016 여러 날에 걸친 시간대 선택은 이른 칸의 시작부터 늦은 칸의 끝까지다") {
            val monday = LocalDate(year = 2026, month = 9, day = 21)
            val tuesday = LocalDate(year = 2026, month = 9, day = 22)
            val mondayTen = timetableTimeSlotAt(date = monday, minuteOfDay = 22 * 60)
            val tuesdayOneThirty = timetableTimeSlotAt(date = tuesday, minuteOfDay = 1 * 60 + 30)
            val mondayLast = timetableTimeSlotAt(date = monday, minuteOfDay = 23 * 60 + 30)
            val caseList =
                listOf(
                    Triple(mondayTen, tuesdayOneThirty, dateTime(monday, 22 to 0) to dateTime(tuesday, 2 to 0)),
                    Triple(tuesdayOneThirty, mondayTen, dateTime(monday, 22 to 0) to dateTime(tuesday, 2 to 0)),
                    Triple(mondayLast, mondayLast, dateTime(monday, 23 to 30) to dateTime(monday, 23 to 59)),
                )

            caseList.forEach { (anchor, current, expected) ->
                TimetableSelection.Time(anchor = anchor, current = current).toEvent() shouldBe
                    TimetableEvent.SelectTime(start = expected.first, endInclusive = expected.second)
            }
        }

        test("날짜 선택은 처음 날짜와 지금 날짜 중 이른 날짜부터 늦은 날짜까지다") {
            val early = LocalDate(year = 2026, month = 9, day = 22)
            val late = LocalDate(year = 2026, month = 9, day = 24)

            TimetableSelection.Date(anchor = late, current = early).toEvent() shouldBe TimetableEvent.SelectDate(dateRange = early..late)
        }

        test("여러 날에 걸친 시간대 선택은 가운데 날짜의 모든 칸을 칠한다") {
            val selection =
                TimetableSelection.Time(
                    anchor = timetableTimeSlotAt(date = LocalDate(year = 2026, month = 9, day = 21), minuteOfDay = 22 * 60),
                    current = timetableTimeSlotAt(date = LocalDate(year = 2026, month = 9, day = 23), minuteOfDay = 60),
                )

            selection.slotIndexRangeOn(LocalDate(year = 2026, month = 9, day = 20)) shouldBe null
            selection.slotIndexRangeOn(LocalDate(year = 2026, month = 9, day = 21)) shouldBe 44..47
            selection.slotIndexRangeOn(LocalDate(year = 2026, month = 9, day = 22)) shouldBe 0..47
            selection.slotIndexRangeOn(LocalDate(year = 2026, month = 9, day = 23)) shouldBe 0..2
        }
    })

private val DATE = LocalDate(year = 2026, month = 9, day = 23)

private fun dateTime(
    date: LocalDate,
    time: Pair<Int, Int>,
): LocalDateTime = LocalDateTime(date = date, time = LocalTime(hour = time.first, minute = time.second))
