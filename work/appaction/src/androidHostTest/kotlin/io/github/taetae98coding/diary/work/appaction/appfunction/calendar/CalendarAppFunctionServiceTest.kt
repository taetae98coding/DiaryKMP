package io.github.taetae98coding.diary.work.appaction.appfunction.calendar

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueListAs
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarBirthday
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarMemo
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionHoliday
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val calendarAppAction = mockk<CalendarAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin { modules(module { single { calendarAppAction } }) }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `캘린더 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList() shouldContainAll listOf(CALENDAR_MEMO_LIST, CALENDAR_BIRTHDAY_LIST, HOLIDAY_LIST)
        }

    @Test
    fun `캘린더 메모는 날짜 범위를 공통 동작에 넘기고 항목 목록을 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val (timedStart, timedEndInclusive) =
                Arb
                    .list(Arb.kotlinxLocalDateTime(), 2..2)
                    .next()
                    .sorted()
                    .map(LocalDateTime::toString)
            val memo =
                AppActionCalendarMemo(
                    id = Arb.kotlinUuid().next().toString(),
                    title = Arb.string().next(),
                    color = Arb.appActionColor().next(),
                    dateTime = AppActionMemoDateTime(allDay = false, start = timedStart, endInclusive = timedEndInclusive),
                )
            coEvery { calendarAppAction.memoList(startDate = any(), endDate = any()) } returns listOf(memo)

            val response =
                tester.execute(name = CALENDAR_MEMO_LIST) {
                    setString(START_DATE, rangeStart)
                    setString(END_DATE, rangeEnd)
                }

            response.returnValueListAs<AppFunctionCalendarMemo>() shouldBe
                listOf(
                    AppFunctionCalendarMemo(
                        id = memo.id,
                        title = memo.title,
                        color = memo.color,
                        dateTime = AppFunctionMemoDateTime(allDay = false, start = timedStart, endInclusive = timedEndInclusive),
                    ),
                )
            coVerify(exactly = 1) { calendarAppAction.memoList(startDate = rangeStart, endDate = rangeEnd) }
        }

    @Test
    fun `캘린더 생일은 항목 목록을 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val birthday =
                AppActionCalendarBirthday(
                    contactId = Arb.kotlinUuid().next().toString(),
                    name = Arb.string().next(),
                    nameQualifier = Arb.string().next(),
                    date = rangeStart,
                )
            coEvery { calendarAppAction.birthdayList(startDate = any(), endDate = any()) } returns listOf(birthday)

            val response =
                tester.execute(name = CALENDAR_BIRTHDAY_LIST) {
                    setString(START_DATE, rangeStart)
                    setString(END_DATE, rangeEnd)
                }

            response.returnValueListAs<AppFunctionCalendarBirthday>() shouldBe
                listOf(AppFunctionCalendarBirthday(contactId = birthday.contactId, name = birthday.name, nameQualifier = birthday.nameQualifier, date = birthday.date))
        }

    @Test
    fun `공휴일은 연도를 공통 동작에 넘기고 항목 목록을 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val year = Arb.int(1900..2100).next()
            val holiday = AppActionHoliday(name = Arb.string().next(), isHoliday = Arb.boolean().next(), start = rangeStart, endInclusive = rangeEnd)
            coEvery { calendarAppAction.holidayList(year = any()) } returns listOf(holiday)

            val response = tester.execute(name = HOLIDAY_LIST) { setInt("year", year) }

            response.returnValueListAs<AppFunctionHoliday>() shouldBe
                listOf(AppFunctionHoliday(name = holiday.name, isHoliday = holiday.isHoliday, start = holiday.start, endInclusive = holiday.endInclusive))
            coVerify(exactly = 1) { calendarAppAction.holidayList(year = year) }
        }

    @Test
    fun `빈 목록도 성공 결과로 돌려준다`() =
        runBlocking<Unit> {
            coEvery { calendarAppAction.holidayList(year = any()) } returns emptyList()

            val response = tester.execute(name = HOLIDAY_LIST) { setInt("year", Arb.int(1900..2100).next()) }

            response.returnValueListAs<AppFunctionHoliday>() shouldBe emptyList()
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val message = Arb.nonBlankString().next()
            coEvery { calendarAppAction.memoList(startDate = any(), endDate = any()) } throws AppActionException.InvalidArgument(message = message)

            val response =
                tester.execute(name = CALENDAR_MEMO_LIST) {
                    setString(START_DATE, rangeEnd)
                    setString(END_DATE, rangeStart)
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            error.errorMessage shouldBe message
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val message = Arb.nonBlankString().next()
            coEvery { calendarAppAction.birthdayList(startDate = any(), endDate = any()) } throws AppActionException.Failure(message = message)

            val response =
                tester.execute(name = CALENDAR_BIRTHDAY_LIST) {
                    setString(START_DATE, rangeStart)
                    setString(END_DATE, rangeEnd)
                }

            val error = response.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            error.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            error.errorMessage shouldBe message
        }
}

private const val START_DATE = "startDate"
private const val END_DATE = "endDate"
private const val CALENDAR_MEMO_LIST = "calendarMemoList"
private const val CALENDAR_BIRTHDAY_LIST = "calendarBirthdayList"
private const val HOLIDAY_LIST = "holidayList"
