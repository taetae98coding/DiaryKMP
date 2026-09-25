package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.saveable.SaverScope
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.Clock

private data class DefaultPeriodTestData(
    val now: LocalTime,
    val startTime: LocalTime,
    val endDayOffset: Int,
    val endTime: LocalTime,
)

class DiaryDateTimeInputStateTest : FunSpec() {
    init {
        test("TC-DIARY-DATE-TIME-INPUT-DOMAIN-001 스위치가 꺼져 있으면 선택된 기간이 없는 것으로 취급된다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.hasDateTime = false

            state.value.shouldBeNull()
        }

        test("종일이면 값이 날짜 범위로 노출된다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = true,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.value shouldBe
                DiaryDateTimeInputValue.AllDay(dateRange = start.date..endInclusive.date)
        }

        test("종일이 아니면 값이 날짜와 시간 범위로 노출된다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.value shouldBe
                DiaryDateTimeInputValue.DateTime(
                    start = start,
                    endInclusive = endInclusive,
                )
        }

        test("종일로 전환하면 시간 정보가 버려지고 날짜만 유지된다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.selectAllDay(true)

            state.start shouldBe LocalDateTime(date = start.date, time = midnight())
            state.endInclusive shouldBe LocalDateTime(date = endInclusive.date, time = midnight())
        }

        test("TC-DIARY-DATE-TIME-INPUT-FEATURE-030 하루짜리 종일 기간을 해제하면 시작이 기본 시각, 종료가 1시간 뒤가 된다") {
            val testDataList =
                listOf(
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 13, minute = 43),
                        startTime = LocalTime(hour = 14, minute = 0),
                        endDayOffset = 0,
                        endTime = LocalTime(hour = 15, minute = 0),
                    ),
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 14, minute = 13),
                        startTime = LocalTime(hour = 14, minute = 30),
                        endDayOffset = 0,
                        endTime = LocalTime(hour = 15, minute = 30),
                    ),
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 14, minute = 0),
                        startTime = LocalTime(hour = 14, minute = 0),
                        endDayOffset = 0,
                        endTime = LocalTime(hour = 15, minute = 0),
                    ),
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 14, minute = 30),
                        startTime = LocalTime(hour = 14, minute = 30),
                        endDayOffset = 0,
                        endTime = LocalTime(hour = 15, minute = 30),
                    ),
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 23, minute = 0),
                        startTime = LocalTime(hour = 23, minute = 0),
                        endDayOffset = 1,
                        endTime = LocalTime(hour = 0, minute = 0),
                    ),
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 23, minute = 30),
                        startTime = LocalTime(hour = 23, minute = 30),
                        endDayOffset = 1,
                        endTime = LocalTime(hour = 0, minute = 30),
                    ),
                    DefaultPeriodTestData(
                        now = LocalTime(hour = 23, minute = 31),
                        startTime = LocalTime(hour = 0, minute = 0),
                        endDayOffset = 0,
                        endTime = LocalTime(hour = 1, minute = 0),
                    ),
                )

            testDataList.forEach { testData ->
                val date = anyDate()
                val state =
                    allDayState(
                        start = date,
                        endInclusive = date,
                        clock = clockOf(LocalDateTime(date = date, time = testData.now)),
                    )

                state.selectAllDay(false)

                state.start shouldBe LocalDateTime(date = date, time = testData.startTime)
                state.endInclusive shouldBe
                    LocalDateTime(
                        date = date.plus(testData.endDayOffset, DateTimeUnit.DAY),
                        time = testData.endTime,
                    )
            }
        }

        test("TC-DIARY-DATE-TIME-INPUT-FEATURE-031 여러 날짜의 종일 기간을 해제하면 두 날짜가 유지되고 시각이 모두 기본 시각이 된다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                allDayState(
                    start = start.date,
                    endInclusive = endInclusive.date,
                    clock = clockOf(LocalDateTime(date = start.date, time = LocalTime(hour = 13, minute = 43))),
                )

            state.selectAllDay(false)

            state.start shouldBe LocalDateTime(date = start.date, time = LocalTime(hour = 14, minute = 0))
            state.endInclusive shouldBe LocalDateTime(date = endInclusive.date, time = LocalTime(hour = 14, minute = 0))
        }

        test("TC-DIARY-DATE-TIME-INPUT-FEATURE-032 종일로 전환한 뒤 다시 해제하면 시간이 기본 시각에서 다시 시작한다") {
            val date = anyDate()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 15)),
                    endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 10, minute = 15)),
                    clock = clockOf(LocalDateTime(date = date, time = LocalTime(hour = 13, minute = 43))),
                )

            state.selectAllDay(true)
            state.selectAllDay(false)

            state.start shouldBe LocalDateTime(date = date, time = LocalTime(hour = 14, minute = 0))
            state.endInclusive shouldBe LocalDateTime(date = date, time = LocalTime(hour = 15, minute = 0))
        }

        test("TC-DIARY-DATE-TIME-INPUT-FEATURE-034 시간이 흐른 뒤 종일을 해제하면 그 시점의 기본 시각을 사용한다") {
            val date = anyDate()
            val state =
                allDayState(
                    start = date,
                    endInclusive = date,
                    clock =
                        clockOf(
                            LocalDateTime(date = date, time = LocalTime(hour = 13, minute = 43)),
                            LocalDateTime(date = date, time = LocalTime(hour = 16, minute = 10)),
                        ),
                )

            state.selectAllDay(false)
            state.selectAllDay(true)
            state.selectAllDay(false)

            state.start shouldBe LocalDateTime(date = date, time = LocalTime(hour = 16, minute = 30))
            state.endInclusive shouldBe LocalDateTime(date = date, time = LocalTime(hour = 17, minute = 30))
        }

        test("TC-DIARY-DATE-TIME-INPUT-DOMAIN-002 이전 기간보다 앞선 날짜·시간 기간을 한 번에 넣으면 보정 없이 그대로 들어간다") {
            val date = anyDate()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = LocalDateTime(date = date.plus(1, DateTimeUnit.DAY), time = LocalTime(hour = 10, minute = 0)),
                    endInclusive = LocalDateTime(date = date.plus(1, DateTimeUnit.DAY), time = LocalTime(hour = 11, minute = 0)),
                )
            val value =
                DiaryDateTimeInputValue.DateTime(
                    start = LocalDateTime(date = date, time = LocalTime(hour = 13, minute = 0)),
                    endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 15, minute = 0)),
                )

            state.select(value)

            state.value shouldBe value
        }

        test("TC-DIARY-DATE-TIME-INPUT-DOMAIN-002 이전 기간보다 뒤인 날짜·시간 기간을 한 번에 넣으면 보정 없이 그대로 들어간다") {
            val date = anyDate()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = LocalDateTime(date = date, time = LocalTime(hour = 20, minute = 0)),
                    endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 21, minute = 0)),
                )
            val value =
                DiaryDateTimeInputValue.DateTime(
                    start = LocalDateTime(date = date.plus(2, DateTimeUnit.DAY), time = LocalTime(hour = 9, minute = 0)),
                    endInclusive = LocalDateTime(date = date.plus(3, DateTimeUnit.DAY), time = LocalTime(hour = 8, minute = 0)),
                )

            state.select(value)

            state.value shouldBe value
        }

        test("TC-DIARY-DATE-TIME-INPUT-DOMAIN-002 기간을 사용하지 않는 종일 상태에 날짜·시간 기간을 넣으면 기본 시각이 끼어들지 않는다") {
            val date = anyDate()
            val state =
                allDayState(
                    start = date,
                    endInclusive = date,
                    clock = clockOf(LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 40))),
                )
            state.hasDateTime = false
            val value =
                DiaryDateTimeInputValue.DateTime(
                    start = LocalDateTime(date = date, time = LocalTime(hour = 13, minute = 0)),
                    endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 15, minute = 0)),
                )

            state.select(value)

            state.hasDateTime shouldBe true
            state.value shouldBe value
        }

        test("TC-DIARY-DATE-TIME-INPUT-DOMAIN-002 날짜·시간 상태에 종일 기간을 넣으면 생성된 날짜 범위가 그대로 들어간다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = LocalDateTime(date = endInclusive.date.plus(1, DateTimeUnit.DAY), time = anyTime()),
                    endInclusive = LocalDateTime(date = endInclusive.date.plus(2, DateTimeUnit.DAY), time = anyTime()),
                )
            val value = DiaryDateTimeInputValue.AllDay(dateRange = start.date..endInclusive.date)

            state.select(value)

            state.value shouldBe value
        }

        test("같은 종일 값을 다시 선택하면 시간이 유지된다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.selectAllDay(false)

            state.start shouldBe start
            state.endInclusive shouldBe endInclusive
        }

        test("시작 날짜를 종료보다 뒤로 선택하면 종료가 시작으로 보정된다") {
            val (start, endInclusive) = anyPeriod()
            val newStartDate = endInclusive.date.plus(1, DateTimeUnit.DAY)
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.selectStartDate(newStartDate)

            state.start shouldBe LocalDateTime(date = newStartDate, time = start.time)
            state.endInclusive shouldBe state.start
        }

        test("시작 시간을 종료보다 뒤로 선택하면 종료가 시작으로 보정된다") {
            val date = anyDate()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 0)),
                    endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 10, minute = 0)),
                )

            state.selectStartTime(LocalTime(hour = 11, minute = 30))

            state.start shouldBe LocalDateTime(date = date, time = LocalTime(hour = 11, minute = 30))
            state.endInclusive shouldBe state.start
        }

        test("종료 날짜를 시작보다 앞으로 선택하면 시작이 종료로 보정된다") {
            val (start, endInclusive) = anyPeriod()
            val newEndDate = start.date.plus(-1, DateTimeUnit.DAY)
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.selectEndDate(newEndDate)

            state.endInclusive shouldBe LocalDateTime(date = newEndDate, time = endInclusive.time)
            state.start shouldBe state.endInclusive
        }

        test("종료 시간을 시작보다 앞으로 선택하면 시작이 종료로 보정된다") {
            val date = anyDate()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 0)),
                    endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 10, minute = 0)),
                )

            state.selectEndTime(LocalTime(hour = 8, minute = 30))

            state.endInclusive shouldBe LocalDateTime(date = date, time = LocalTime(hour = 8, minute = 30))
            state.start shouldBe state.endInclusive
        }

        test("기간 관계를 유지하는 선택은 다른 항목을 바꾸지 않는다") {
            val (start, endInclusive) = anyPeriod()
            val newStartDate = start.date.plus(-1, DateTimeUnit.DAY)
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = start,
                    endInclusive = endInclusive,
                )

            state.selectStartDate(newStartDate)

            state.start shouldBe LocalDateTime(date = newStartDate, time = start.time)
            state.endInclusive shouldBe endInclusive
        }

        test("Saver가 저장한 상태를 동일하게 복원한다") {
            val (start, endInclusive) = anyPeriod()
            val state =
                DiaryDateTimeInputState(
                    hasDateTime = fixtureMonkey.giveMeOne<Boolean>(),
                    isAllDay = fixtureMonkey.giveMeOne<Boolean>(),
                    start = start,
                    endInclusive = endInclusive,
                )

            val saved = with(DiaryDateTimeInputState.Saver) { SaverScope { true }.save(state) }
            val restored = DiaryDateTimeInputState.Saver.restore(checkNotNull(saved))

            checkNotNull(restored)
            restored.hasDateTime shouldBe state.hasDateTime
            restored.isAllDay shouldBe state.isAllDay
            restored.start shouldBe state.start
            restored.endInclusive shouldBe state.endInclusive
        }

        test("기본 시각은 30분 단위로 올린다") {
            LocalTime(hour = 0, minute = 0).toDefaultTime() shouldBe LocalTime(hour = 0, minute = 0)
            LocalTime(hour = 0, minute = 0, second = 1).toDefaultTime() shouldBe LocalTime(hour = 0, minute = 30)
            LocalTime(hour = 0, minute = 29, second = 59).toDefaultTime() shouldBe LocalTime(hour = 0, minute = 30)
            LocalTime(hour = 12, minute = 30, nanosecond = 1).toDefaultTime() shouldBe LocalTime(hour = 13, minute = 0)
            LocalTime(hour = 23, minute = 59, second = 59).toDefaultTime() shouldBe LocalTime(hour = 0, minute = 0)
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun midnight(): LocalTime = LocalTime(hour = 0, minute = 0)

        private fun anyDate(): LocalDate = LocalDate.fromEpochDays(fixtureMonkey.giveMeOne<Int>().mod(40_000).toLong())

        private fun anyTime(): LocalTime =
            LocalTime(
                hour = fixtureMonkey.giveMeOne<Int>().mod(24),
                minute = fixtureMonkey.giveMeOne<Int>().mod(59) + 1,
            )

        private fun anyPeriod(): Pair<LocalDateTime, LocalDateTime> {
            val first = LocalDateTime(date = anyDate(), time = anyTime())
            val second = LocalDateTime(date = first.date.plus(fixtureMonkey.giveMeOne<Int>().mod(30) + 1, DateTimeUnit.DAY), time = anyTime())

            return first to second
        }

        private fun allDayState(
            start: LocalDate,
            endInclusive: LocalDate,
            clock: Clock,
        ): DiaryDateTimeInputState =
            DiaryDateTimeInputState(
                hasDateTime = true,
                isAllDay = true,
                start = LocalDateTime(date = start, time = midnight()),
                endInclusive = LocalDateTime(date = endInclusive, time = midnight()),
                clock = clock,
            )

        private fun clockOf(vararg now: LocalDateTime): Clock {
            val clock = mockk<Clock>()

            every { clock.now() } returnsMany now.map { it.toInstant(TimeZone.currentSystemDefault()) }

            return clock
        }
    }
}
