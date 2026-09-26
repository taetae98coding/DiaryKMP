package io.github.taetae98coding.diary.compose.timetable

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class TimetableLayoutTest :
    FunSpec({
        test("TC-TIMETABLE-DOMAIN-002 시간이 겹치지 않는 시각 아이템은 날짜 열 너비 전체를 쓴다") {
            val a = timeItem(start = "10:00", end = "11:00")
            val b = timeItem(start = "11:00", end = "12:00")

            listOf(a, b).placement() shouldBe mapOf(a.key to (0 to 1), b.key to (0 to 1))
        }

        test("TC-TIMETABLE-DOMAIN-003 시간이 겹치는 시각 아이템은 칸을 나누어 나란히 놓인다") {
            val a = timeItem(start = "10:00", end = "12:00")
            val b = timeItem(start = "11:00", end = "13:00")
            val c = timeItem(start = "12:00", end = "14:00")

            listOf(a, b, c).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2), c.key to (0 to 2))
        }

        test("TC-TIMETABLE-DOMAIN-004 시작이 같으면 끝이 늦은 아이템을 먼저 놓는다") {
            val a = timeItem(start = "10:00", end = "11:00")
            val b = timeItem(start = "10:00", end = "12:00")

            listOf(a, b).placement() shouldBe mapOf(b.key to (0 to 2), a.key to (1 to 2))
        }

        test("TC-TIMETABLE-DOMAIN-005 겹치지 않는 묶음은 서로 칸 수에 영향을 주지 않는다") {
            val a = timeItem(start = "09:00", end = "10:00")
            val b = timeItem(start = "09:30", end = "10:30")
            val c = timeItem(start = "15:00", end = "16:00")

            listOf(a, b, c).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2), c.key to (0 to 1))
        }

        test("TC-TIMETABLE-DOMAIN-006 시각 아이템은 시작 시각부터 종료 시각까지를 차지하고 최소 30분을 차지한다") {
            val caseList =
                listOf(
                    Triple("10:00", "11:30", minute(hour = 10)..minute(hour = 11, minute = 30)),
                    Triple("10:00", "10:00", minute(hour = 10)..minute(hour = 10, minute = 30)),
                    Triple("23:50", "23:55", minute(hour = 23, minute = 30)..MINUTES_PER_DAY),
                )

            caseList.forEach { (start, end, expected) ->
                val placed = listOf(timeItem(start = start, end = end)).placeOn(date = DATE).single()

                placed.startMinute..placed.endMinute shouldBe expected
            }
        }

        test("TC-TIMETABLE-DOMAIN-007 종일 아이템은 표시 날짜 안쪽만 차지하고 겹치면 다른 줄에 놓인다") {
            val a = allDayItem(start = date(day = 18), end = date(day = 22))
            val b = allDayItem(start = date(day = 23), end = date(day = 24))
            val c = allDayItem(start = date(day = 21), end = date(day = 28))

            val placedList = listOf(a, b, c).placeIn(dateRange = date(day = 20)..date(day = 26))

            placedList.associate { it.item.key to (it.dateRange to it.row) } shouldBe
                mapOf(
                    a.key to (date(day = 20)..date(day = 22) to 0),
                    b.key to (date(day = 23)..date(day = 24) to 0),
                    c.key to (date(day = 21)..date(day = 26) to 1),
                )
        }

        test("TC-TIMETABLE-DOMAIN-011 잘린 기간의 시작이 같으면 끝이 늦은 종일 아이템을 위쪽 줄에 놓는다") {
            val a = allDayItem(start = date(day = 20), end = date(day = 21))
            val b = allDayItem(start = date(day = 20), end = date(day = 23))

            listOf(a, b).rowIn() shouldBe mapOf(b.key to 0, a.key to 1)
        }

        test("TC-TIMETABLE-DOMAIN-012 잘린 기간이 같으면 잘리기 전 기간으로 줄 순서를 정한다") {
            val a = allDayItem(start = date(day = 19), end = date(day = 28))
            val b = allDayItem(start = date(day = 18), end = date(day = 27))

            listOf(a, b).rowIn() shouldBe mapOf(b.key to 0, a.key to 1)
        }

        test("TC-TIMETABLE-DOMAIN-013 기간이 모두 같은 종일 아이템은 먼저 지정한 아이템을 위쪽 줄에 놓는다") {
            val a = allDayItem(start = date(day = 21), end = date(day = 22))
            val b = allDayItem(start = date(day = 21), end = date(day = 22))

            listOf(a, b).rowIn() shouldBe mapOf(a.key to 0, b.key to 1)
        }

        test("TC-TIMETABLE-DOMAIN-008 차지하는 시간대가 모두 같으면 먼저 지정한 아이템부터 놓는다") {
            val a = timeItem(start = "10:00", end = "11:00")
            val b = timeItem(start = "10:00", end = "11:00")

            listOf(a, b).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2))
        }

        test("TC-TIMETABLE-DOMAIN-009 최소 길이로 늘어난 시간대도 겹침에 포함한다") {
            val a = timeItem(start = "10:00", end = "10:00")
            val b = timeItem(start = "10:10", end = "11:00")

            listOf(a, b).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2))
        }

        test("TC-TIMETABLE-DOMAIN-010 다른 날짜의 시각 아이템과 표시 날짜와 겹치지 않는 종일 아이템은 놓이지 않는다") {
            listOf(timeItem(start = "10:00", end = "11:00")).placeOn(date = date(day = 24)).shouldBeEmpty()
            listOf(allDayItem(start = date(day = 1), end = date(day = 2))).placeIn(dateRange = DATE..DATE).shouldBeEmpty()
        }
    })

private val DATE = date(day = 23)

private fun List<TimetableTimeItem>.placement(): Map<Any, Pair<Int, Int>> = placeOn(date = DATE).associate { it.item.key to (it.column to it.columnCount) }

private fun List<TimetableAllDayItem>.rowIn(): Map<Any, Int> = placeIn(dateRange = date(day = 20)..date(day = 26)).associate { it.item.key to it.row }

private fun minute(
    hour: Int,
    minute: Int = 0,
): Int = hour * MINUTES_PER_HOUR + minute

private fun date(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private fun timeItem(
    start: String,
    end: String,
): TimetableTimeItem =
    TimetableTimeItem(
        date = DATE,
        startTime = LocalTime.parse(start),
        endTime = LocalTime.parse(end),
        key = fixtureMonkey.giveMeOne<Uuid>(),
        content = {},
    )

private fun allDayItem(
    start: LocalDate,
    end: LocalDate,
): TimetableAllDayItem =
    TimetableAllDayItem(
        dateRange = start..end,
        key = fixtureMonkey.giveMeOne<Uuid>(),
        content = {},
    )
