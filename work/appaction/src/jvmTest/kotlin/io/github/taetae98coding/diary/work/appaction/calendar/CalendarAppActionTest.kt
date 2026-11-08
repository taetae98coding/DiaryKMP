package io.github.taetae98coding.diary.work.appaction.calendar

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.contact.calendarContactBirthday
import io.github.taetae98coding.diary.core.testing.holiday.holiday
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.core.testing.memo.memoDateTime
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class CalendarAppActionTest :
    FunSpec({
        test("캘린더 메모는 받은 날짜 범위로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val dateRange = Arb.localDateRange().next()
                val memoList = List(size = Arb.int(1..3).next()) { Arb.calendarMemo(dateTime = Arb.memoDateTime().next()).next() }
                val getCalendarMemoUseCase = mockk<GetCalendarMemoUseCase>()
                every { getCalendarMemoUseCase(parameter = any()) } returns flowOf(Result.success(memoList))
                val action = calendarAppActionOf(getCalendarMemoUseCase)

                action.memoList(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) shouldBe
                    memoList.map { memo ->
                        AppActionCalendarMemo(
                            id = memo.id.toString(),
                            title = memo.title,
                            color = memo.color.expectedHex(),
                            dateTime = memo.dateTime.expected(),
                        )
                    }
                verify(exactly = 1) { getCalendarMemoUseCase(parameter = dateRange) }
            }
        }

        test("캘린더 생일은 받은 날짜 범위로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val dateRange = Arb.localDateRange().next()
                val birthdayList = Arb.list(Arb.calendarContactBirthday(), 1..3).next()
                val getCalendarContactBirthdayUseCase = mockk<GetCalendarContactBirthdayUseCase>()
                every { getCalendarContactBirthdayUseCase(parameter = any()) } returns flowOf(Result.success(birthdayList))
                val action = calendarAppActionOf(getCalendarContactBirthdayUseCase)

                action.birthdayList(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) shouldBe
                    birthdayList.map { birthday ->
                        AppActionCalendarBirthday(
                            contactId = birthday.contactId.toString(),
                            name = birthday.name,
                            nameQualifier = birthday.nameQualifier,
                            date = birthday.date.toString(),
                        )
                    }
                verify(exactly = 1) { getCalendarContactBirthdayUseCase(parameter = dateRange) }
            }
        }

        test("공휴일은 받은 연도로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val year = Arb.int(1900..2100).next()
                val holidayList = Arb.list(Arb.holiday(), 1..3).next()
                val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getCalendarHolidayUseCase(parameter = any()) } returns flowOf(Result.success(holidayList))
                val action = calendarAppActionOf(getCalendarHolidayUseCase)

                action.holidayList(year = year) shouldBe
                    holidayList.map { holiday ->
                        AppActionHoliday(
                            name = holiday.name,
                            isHoliday = holiday.isHoliday,
                            start = holiday.dateRange.start.toString(),
                            endInclusive = holiday.dateRange.endInclusive.toString(),
                        )
                    }
                verify(exactly = 1) { getCalendarHolidayUseCase(parameter = year) }
            }
        }

        test("잘못된 날짜나 시작일보다 앞선 마지막 날짜는 조회하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val dateRange = Arb.localDateRange().next()
                val getCalendarMemoUseCase = mockk<GetCalendarMemoUseCase>()
                val getCalendarContactBirthdayUseCase = mockk<GetCalendarContactBirthdayUseCase>()
                val action = calendarAppActionOf(getCalendarMemoUseCase, getCalendarContactBirthdayUseCase)
                val start = dateRange.start.toString()
                val beforeStart = dateRange.start.minusDay().toString()

                shouldThrow<AppActionException.InvalidArgument> { action.memoList(startDate = NOT_DATE, endDate = start) }.message shouldBe
                    "'startDate' must be a date like 2026-01-31."
                shouldThrow<AppActionException.InvalidArgument> { action.memoList(startDate = start, endDate = NOT_DATE) }.message shouldBe
                    "'endDate' must be a date like 2026-01-31."
                shouldThrow<AppActionException.InvalidArgument> { action.birthdayList(startDate = start, endDate = beforeStart) }.message shouldBe
                    "'endDate' must not be before 'startDate'."

                verify(exactly = 0) { getCalendarMemoUseCase(parameter = any()) }
                verify(exactly = 0) { getCalendarContactBirthdayUseCase(parameter = any()) }
            }
        }

        test("시작일과 마지막 날짜가 같으면 그 하루로 조회한다") {
            runTest {
                val date = Arb.localDateRange().next().start
                val getCalendarMemoUseCase = mockk<GetCalendarMemoUseCase>()
                every { getCalendarMemoUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                val action = calendarAppActionOf(getCalendarMemoUseCase)

                action.memoList(startDate = date.toString(), endDate = date.toString()) shouldBe emptyList()
                verify(exactly = 1) { getCalendarMemoUseCase(parameter = date..date) }
            }
        }

        test("캘린더 디데이는 받은 날짜 범위로 조회해 날짜순으로 문구의 종류와 숫자를 담아 돌려준다") {
            runTest {
                val ddayId = Arb.kotlinUuid().next()
                val title = Arb.nonBlankString().next()
                val color = Arb.long().next()
                val labelList =
                    listOf(
                        CalendarDDayLabel.BaseDay to AppActionCalendarDDayLabel(kind = "base_day", count = null),
                        CalendarDDayLabel.Days(days = 100) to AppActionCalendarDDayLabel(kind = "days", count = 100),
                        CalendarDDayLabel.Months(months = 6) to AppActionCalendarDDayLabel(kind = "months", count = 6),
                        CalendarDDayLabel.Years(years = 1) to AppActionCalendarDDayLabel(kind = "years", count = 1),
                    )
                val dateList = List(labelList.size) { index -> LocalDate(2026, 1, 1 + index) }
                val ddayList = labelList.mapIndexed { index, (label, _) -> CalendarDDay(ddayId = ddayId, title = title, color = color, date = dateList[index], label = label) }
                val getCalendarDDayUseCase = mockk<GetCalendarDDayUseCase>()
                every { getCalendarDDayUseCase(parameter = any()) } returns flowOf(Result.success(ddayList.reversed()))
                val action = calendarAppActionOf(getCalendarDDayUseCase)

                action.ddayList(startDate = "2026-01-01", endDate = "2026-01-31") shouldBe
                    labelList.mapIndexed { index, (_, label) ->
                        AppActionCalendarDDay(ddayId = ddayId.toString(), title = title, color = color.expectedHex(), date = dateList[index].toString(), label = label)
                    }
                verify(exactly = 1) { getCalendarDDayUseCase(parameter = LocalDate(2026, 1, 1)..LocalDate(2026, 1, 31)) }
            }
        }

        test("캘린더 조회가 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getCalendarHolidayUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = calendarAppActionOf(getCalendarHolidayUseCase)

                shouldThrow<AppActionException.Failure> { action.holidayList(year = Arb.int(1900..2100).next()) }.message shouldBe "IllegalStateException: $message"
            }
        }
    })

private const val NOT_DATE = "not-date"
private const val RGB = 0xFFFFFFL

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)

private fun LocalDate.minusDay(): LocalDate = LocalDate.fromEpochDays(toEpochDays() - 1)

private fun MemoDateTime.expected(): AppActionMemoDateTime =
    when (this) {
        is MemoDateTime.AllDay -> AppActionMemoDateTime(allDay = true, start = dateRange.start.toString(), endInclusive = dateRange.endInclusive.toString())
        is MemoDateTime.DateTime -> AppActionMemoDateTime(allDay = false, start = start.toString(), endInclusive = endInclusive.toString())
    }
