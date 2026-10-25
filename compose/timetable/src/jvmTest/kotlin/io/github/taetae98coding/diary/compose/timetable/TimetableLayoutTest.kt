package io.github.taetae98coding.diary.compose.timetable

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class TimetableLayoutTest :
    FunSpec({
        test("TC-0ce8e213-0f1e-4bef-96cc-da637accfdc1 시간이 겹치지 않는 시각 아이템은 날짜 열 너비 전체를 쓴다") {
            val a = timeItem(start = "10:00", end = "11:00")
            val b = timeItem(start = "11:00", end = "12:00")

            listOf(a, b).placement() shouldBe mapOf(a.key to (0 to 1), b.key to (0 to 1))
        }

        test("TC-4fc58de6-2612-4f97-a2c1-2ffd76ab3227 시간이 겹치는 시각 아이템은 칸을 나누어 나란히 놓인다") {
            val a = timeItem(start = "10:00", end = "12:00")
            val b = timeItem(start = "11:00", end = "13:00")
            val c = timeItem(start = "12:00", end = "14:00")

            listOf(a, b, c).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2), c.key to (0 to 2))
        }

        test("TC-c68a6b49-d0aa-4833-81eb-c6b16b427ee4 시작이 같으면 끝이 늦은 아이템을 먼저 놓는다") {
            val a = timeItem(start = "10:00", end = "11:00")
            val b = timeItem(start = "10:00", end = "12:00")

            listOf(a, b).placement() shouldBe mapOf(b.key to (0 to 2), a.key to (1 to 2))
        }

        test("TC-7411a881-8e58-4997-9bc9-a7f83a612f3b 겹치지 않는 묶음은 서로 칸 수에 영향을 주지 않는다") {
            val a = timeItem(start = "09:00", end = "10:00")
            val b = timeItem(start = "09:30", end = "10:30")
            val c = timeItem(start = "15:00", end = "16:00")

            listOf(a, b, c).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2), c.key to (0 to 1))
        }

        test("TC-f1f16d7c-3d4b-4892-9801-c9665715fd64 시각 아이템은 시작 시각부터 종료 시각까지를 차지하고 최소 30분을 차지한다") {
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

        test("TC-96bafd2a-3bfc-4942-811d-f538d8609caa 종일 아이템은 표시 날짜 안쪽만 차지하고 겹치면 다른 줄에 놓인다") {
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

        test("TC-0fe12c51-c376-4a57-93a8-9ca358232d71 잘린 기간의 시작이 같으면 끝이 늦은 종일 아이템을 위쪽 줄에 놓는다") {
            val a = allDayItem(start = date(day = 20), end = date(day = 21))
            val b = allDayItem(start = date(day = 20), end = date(day = 23))

            listOf(a, b).rowIn() shouldBe mapOf(b.key to 0, a.key to 1)
        }

        test("TC-ef494801-7f4e-4aa8-bc42-2ec2f3381c39 잘린 기간이 같으면 잘리기 전 기간으로 줄 순서를 정한다") {
            val a = allDayItem(start = date(day = 19), end = date(day = 28))
            val b = allDayItem(start = date(day = 18), end = date(day = 27))

            listOf(a, b).rowIn() shouldBe mapOf(b.key to 0, a.key to 1)
        }

        test("TC-490fdf03-15ff-43bc-bfe7-cc48930c8517 기간이 모두 같은 종일 아이템은 먼저 지정한 아이템을 위쪽 줄에 놓는다") {
            val a = allDayItem(start = date(day = 21), end = date(day = 22))
            val b = allDayItem(start = date(day = 21), end = date(day = 22))

            listOf(a, b).rowIn() shouldBe mapOf(a.key to 0, b.key to 1)
        }

        test("TC-105cbff4-a75e-4331-8d98-96428b748141 차지하는 시간대가 모두 같으면 먼저 지정한 아이템부터 놓는다") {
            val a = timeItem(start = "10:00", end = "11:00")
            val b = timeItem(start = "10:00", end = "11:00")

            listOf(a, b).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2))
        }

        test("TC-1ffc5f80-4ed0-468d-97d0-8c225dd1e489 최소 길이로 늘어난 시간대도 겹침에 포함한다") {
            val a = timeItem(start = "10:00", end = "10:00")
            val b = timeItem(start = "10:10", end = "11:00")

            listOf(a, b).placement() shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2))
        }

        test("TC-3c25456d-71c4-4848-9283-4fe9132a219c 다른 날짜의 시각 아이템과 표시 날짜와 겹치지 않는 종일 아이템은 놓이지 않는다") {
            listOf(timeItem(start = "10:00", end = "11:00")).placeOn(date = date(day = 24)).shouldBeEmpty()
            listOf(allDayItem(start = date(day = 1), end = date(day = 2))).placeIn(dateRange = DATE..DATE).shouldBeEmpty()
        }

        test("TC-9dd94c3e-fcb0-40db-8ee7-5609c886cdad 여러 날에 걸친 시각 아이템은 날짜마다 그 날짜에 해당하는 시간대를 차지한다") {
            val caseList =
                listOf(
                    timeItem(start = dateTime(day = 21, hour = 22), endInclusive = dateTime(day = 23, hour = 1)) to
                        listOf(minute(hour = 22)..MINUTES_PER_DAY, 0..MINUTES_PER_DAY, 0..minute(hour = 1)),
                    timeItem(start = dateTime(day = 21, hour = 22), endInclusive = dateTime(day = 22, hour = 0)) to
                        listOf(minute(hour = 22)..MINUTES_PER_DAY, null, null),
                    timeItem(start = dateTime(day = 21, hour = 23, minute = 50), endInclusive = dateTime(day = 22, hour = 0, minute = 10)) to
                        listOf(minute(hour = 23, minute = 30)..MINUTES_PER_DAY, 0..minute(hour = 0, minute = 30), null),
                )

            caseList.forEach { (item, expected) ->
                listOf(21, 22, 23).map { day ->
                    listOf(item).placeOn(date = date(day = day)).singleOrNull()?.let { it.startMinute..it.endMinute }
                } shouldBe expected
            }
        }

        test("TC-188c2e3c-7437-4fff-b9fd-0c38027cf108 여러 날에 걸친 시각 아이템은 날짜마다 따로 배치된다") {
            val a = timeItem(start = dateTime(day = 21, hour = 22), endInclusive = dateTime(day = 22, hour = 10))
            val b = timeItem(start = dateTime(day = 21, hour = 22, minute = 30), endInclusive = dateTime(day = 21, hour = 23))
            val c = timeItem(start = dateTime(day = 22, hour = 12), endInclusive = dateTime(day = 22, hour = 13))
            val itemList = listOf(a, b, c)

            itemList.placement(date = date(day = 21)) shouldBe mapOf(a.key to (0 to 2), b.key to (1 to 2))
            itemList.placement(date = date(day = 22)) shouldBe mapOf(a.key to (0 to 1), c.key to (0 to 1))
        }

        test("TC-0a7c98cf-8ccd-4dfc-978d-cc3b9e7ef8b9 표시 날짜 밖으로 이어지는 시각 아이템은 표시 날짜 안쪽 부분만 놓인다") {
            val a = timeItem(start = dateTime(day = 26, hour = 22), endInclusive = dateTime(day = 27, hour = 2))
            val b = timeItem(start = dateTime(day = 19, hour = 20), endInclusive = dateTime(day = 20, hour = 0))

            val placedByDate = (date(day = 20)..date(day = 26)).associateWith { date -> listOf(a, b).placeOn(date = date) }

            placedByDate.filterValues { it.isNotEmpty() }.mapValues { (_, placedList) ->
                placedList.map { Triple(it.item.key, it.startMinute, it.endMinute) }
            } shouldBe mapOf(date(day = 26) to listOf(Triple(a.key, minute(hour = 22), MINUTES_PER_DAY)))
        }
    })

private val DATE = date(day = 23)

private fun List<TimetableTimeItem>.placement(date: LocalDate = DATE): Map<Any, Pair<Int, Int>> = placeOn(date = date).associate { it.item.key to (it.column to it.columnCount) }

private fun List<TimetableAllDayItem>.rowIn(): Map<Any, Int> = placeIn(dateRange = date(day = 20)..date(day = 26)).associate { it.item.key to it.row }

private fun minute(
    hour: Int,
    minute: Int = 0,
): Int = hour * MINUTES_PER_HOUR + minute

private fun date(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private fun dateTime(
    day: Int,
    hour: Int,
    minute: Int = 0,
): LocalDateTime = LocalDateTime(date = date(day = day), time = LocalTime(hour = hour, minute = minute))

private fun timeItem(
    start: String,
    end: String,
): TimetableTimeItem =
    timeItem(
        start = LocalDateTime(date = DATE, time = LocalTime.parse(start)),
        endInclusive = LocalDateTime(date = DATE, time = LocalTime.parse(end)),
    )

private fun timeItem(
    start: LocalDateTime,
    endInclusive: LocalDateTime,
): TimetableTimeItem =
    TimetableTimeItem(
        start = start,
        endInclusive = endInclusive,
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
