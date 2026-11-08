package io.github.taetae98coding.diary.work.appaction.appfunction.dday

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.calendar.AppFunctionCalendarDDay
import io.github.taetae98coding.diary.work.appaction.appfunction.calendar.AppFunctionCalendarDDayLabel
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueAs
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueListAs
import io.github.taetae98coding.diary.work.appaction.appfunction.search.AppFunctionSearchDDayPage
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDay
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDayLabel
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.core.appActionColor
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDay
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayCount
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayDetail
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayOverflowPolicyValue
import io.github.taetae98coding.diary.work.appaction.dday.DDayAppAction
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
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
class DDayAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val ddayAppAction = mockk<DDayAppAction>()
    private val searchAppAction = mockk<SearchAppAction>()
    private val calendarAppAction = mockk<CalendarAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { ddayAppAction }
                    single { searchAppAction }
                    single { calendarAppAction }
                },
            )
        }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `디데이 동작을 모두 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("dday") } shouldContainExactlyInAnyOrder
                listOf("ddayList", "ddayGet", "ddayAdd", "ddayUpdate", "ddayDelete", "ddayRestore", "ddayTagAdd", "ddayTagRemove")
            tester.functionNameList() shouldContainAll listOf("searchDday", "calendarDdayList")
        }

    @Test
    fun `디데이 목록은 공통 동작의 페이지를 계산한 값과 함께 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val dday = appActionDDay(count = AppActionDDayCount(status = "upcoming", remainingDays = Arb.int(1..1000).next(), passedDays = null, years = 0, months = Arb.int(0..11).next()))
            val cursor = Arb.int(1..1000).next().toString()
            coEvery { ddayAppAction.list(cursor = any()) } returns AppActionPage(items = listOf(dday), nextCursor = null)

            val response = tester.execute(name = "ddayList") { setString("cursor", cursor) }

            response.returnValueAs<AppFunctionDDayPage>() shouldBe AppFunctionDDayPage(items = listOf(dday.expected()), nextCursor = null)
            coVerify(exactly = 1) { ddayAppAction.list(cursor = cursor) }
        }

    @Test
    fun `디데이 상세는 당일이면 상태만 담아 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val dday = appActionDDay(count = AppActionDDayCount(status = "today", remainingDays = null, passedDays = null, years = null, months = null))
            coEvery { ddayAppAction.get(ddayId = dday.id) } returns AppActionDDayDetail(dday = dday, tags = emptyList())

            val response = tester.execute(name = "ddayGet") { setString(DDAY_ID, dday.id) }

            response.returnValueAs<AppFunctionDDayDetail>() shouldBe AppFunctionDDayDetail(dday = dday.expected(), tags = emptyList())
        }

    @Test
    fun `디데이 추가는 받은 값을 공통 동작에 넘기고 담지 않은 값은 비워 둔다`() =
        runBlocking<Unit> {
            val title = Arb.nonBlankString().next()
            val addedId = Arb.kotlinUuid().next().toString()
            coEvery {
                ddayAppAction.add(
                    title = any(),
                    description = any(),
                    color = any(),
                    baseDate = any(),
                    countsBaseDay = any(),
                    repeats = any(),
                    endDate = any(),
                    overflowPolicy = any(),
                    tagIds = any(),
                )
            } returns AppActionId(id = addedId)

            val response =
                tester.execute(name = "ddayAdd") {
                    setString("title", title)
                    setStringList("repeats", requestedRepeatList)
                    setString("overflowPolicy", AppActionDDayOverflowPolicyValue.SKIP)
                }

            response.returnValueAs<AppFunctionId>() shouldBe AppFunctionId(id = addedId)
            coVerify(exactly = 1) {
                ddayAppAction.add(
                    title = title,
                    description = null,
                    color = null,
                    baseDate = null,
                    countsBaseDay = null,
                    repeats = requestedRepeatList,
                    endDate = null,
                    overflowPolicy = AppActionDDayOverflowPolicyValue.SKIP,
                    tagIds = emptyList(),
                )
            }
        }

    @Test
    fun `디데이 수정은 반복 간격을 담지 않으면 null로, 종료일 지우기를 그대로 넘긴다`() =
        runBlocking<Unit> {
            val ddayId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery {
                ddayAppAction.update(
                    ddayId = any(),
                    title = any(),
                    description = any(),
                    color = any(),
                    baseDate = any(),
                    countsBaseDay = any(),
                    repeats = any(),
                    endDate = any(),
                    clearEndDate = any(),
                    overflowPolicy = any(),
                )
            } returns AppActionChangedCount(changedCount = changedCount)

            val response =
                tester.execute(name = "ddayUpdate") {
                    setString(DDAY_ID, ddayId)
                    setBoolean("clearEndDate", true)
                }

            response.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            coVerify(exactly = 1) {
                ddayAppAction.update(
                    ddayId = ddayId,
                    title = null,
                    description = null,
                    color = null,
                    baseDate = null,
                    countsBaseDay = null,
                    repeats = null,
                    endDate = null,
                    clearEndDate = true,
                    overflowPolicy = null,
                )
            }
        }

    @Test
    fun `삭제는 바뀐 수를, 태그 연결은 완료를 돌려준다`() =
        runBlocking<Unit> {
            val ddayId = Arb.kotlinUuid().next().toString()
            val tagId = Arb.kotlinUuid().next().toString()
            val changedCount = Arb.int(0..1).next()
            coEvery { ddayAppAction.delete(ddayId = ddayId) } returns AppActionChangedCount(changedCount = changedCount)
            coEvery { ddayAppAction.tagAdd(ddayId = ddayId, tagId = tagId) } returns AppActionDone(done = true)

            tester.execute(name = "ddayDelete") { setString(DDAY_ID, ddayId) }.returnValueAs<AppFunctionChangedCount>() shouldBe AppFunctionChangedCount(changedCount = changedCount)
            tester
                .execute(name = "ddayTagAdd") {
                    setString(DDAY_ID, ddayId)
                    setString("tagId", tagId)
                }.returnValueAs<AppFunctionDone>() shouldBe AppFunctionDone(done = true)
        }

    @Test
    fun `디데이 검색은 검색어와 정렬을 넘기고 페이지를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val query = Arb.string().next()
            val dday = appActionDDay(count = AppActionDDayCount(status = "passed", remainingDays = null, passedDays = Arb.int(1..1000).next(), years = 1, months = 2))
            coEvery { searchAppAction.dday(query = any(), sort = any(), cursor = any()) } returns AppActionPage(items = listOf(dday), nextCursor = null)

            val response =
                tester.execute(name = "searchDday") {
                    setString("query", query)
                    setString("sort", AppActionSortValue.TITLE)
                }

            response.returnValueAs<AppFunctionSearchDDayPage>() shouldBe AppFunctionSearchDDayPage(items = listOf(dday.expected()), nextCursor = null)
            coVerify(exactly = 1) { searchAppAction.dday(query = query, sort = AppActionSortValue.TITLE, cursor = null) }
        }

    @Test
    fun `캘린더 디데이는 날짜 범위를 넘기고 문구의 종류와 숫자를 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val ddayId = Arb.kotlinUuid().next().toString()
            val title = Arb.string().next()
            val color = Arb.appActionColor().next()
            val dday = AppActionCalendarDDay(ddayId = ddayId, title = title, color = color, date = HUNDREDTH_DAY, label = AppActionCalendarDDayLabel(kind = "days", count = 100))
            val baseDay = AppActionCalendarDDay(ddayId = ddayId, title = title, color = color, date = RANGE_START, label = AppActionCalendarDDayLabel(kind = "base_day", count = null))
            coEvery { calendarAppAction.ddayList(startDate = any(), endDate = any()) } returns listOf(baseDay, dday)

            val response =
                tester.execute(name = "calendarDdayList") {
                    setString("startDate", RANGE_START)
                    setString("endDate", RANGE_END)
                }

            response.returnValueListAs<AppFunctionCalendarDDay>() shouldBe
                listOf(
                    AppFunctionCalendarDDay(ddayId = ddayId, title = title, color = color, date = RANGE_START, label = AppFunctionCalendarDDayLabel(kind = "base_day", count = null)),
                    AppFunctionCalendarDDay(ddayId = ddayId, title = title, color = color, date = HUNDREDTH_DAY, label = AppFunctionCalendarDDayLabel(kind = "days", count = 100)),
                )
            coVerify(exactly = 1) { calendarAppAction.ddayList(startDate = RANGE_START, endDate = RANGE_END) }
        }

    @Test
    fun `인자 오류는 잘못된 인자 실패로, 앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val invalidMessage = Arb.nonBlankString().next()
            val failureMessage = Arb.nonBlankString().next()
            coEvery { ddayAppAction.restore(ddayId = any()) } throws AppActionException.InvalidArgument(message = invalidMessage)
            coEvery { ddayAppAction.tagRemove(ddayId = any(), tagId = any()) } throws AppActionException.Failure(message = failureMessage)

            val invalidError = tester.execute(name = "ddayRestore") { setString(DDAY_ID, "not-id") }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>().error
            val failureError =
                tester
                    .execute(name = "ddayTagRemove") {
                        setString(DDAY_ID, Arb.kotlinUuid().next().toString())
                        setString("tagId", Arb.kotlinUuid().next().toString())
                    }.shouldBeInstanceOf<ExecuteAppFunctionResponse.Error>()
                    .error

            invalidError.shouldBeInstanceOf<AppFunctionInvalidArgumentException>()
            invalidError.errorMessage shouldBe invalidMessage
            failureError.shouldBeInstanceOf<AppFunctionAppUnknownException>()
            failureError.errorMessage shouldBe failureMessage
        }
}

private const val DDAY_ID = "ddayId"
private const val RANGE_START = "2026-01-01"
private const val RANGE_END = "2026-12-31"
private const val HUNDREDTH_DAY = "2026-04-10"

private val requestedRepeatList: List<String> = listOf("P100D", "P1Y")

private fun appActionDDay(count: AppActionDDayCount): AppActionDDay =
    AppActionDDay(
        id = Arb.kotlinUuid().next().toString(),
        title = Arb.string().next(),
        description = Arb.string().next(),
        color = Arb.appActionColor().next(),
        baseDate = "2026-01-31",
        countsBaseDay = Arb.boolean().next(),
        repeats = requestedRepeatList,
        endDate = null,
        overflowPolicy = AppActionDDayOverflowPolicyValue.CLAMP_TO_MONTH_END,
        createdAt = Arb.string().next(),
        updatedAt = Arb.string().next(),
        count = count,
    )

private fun AppActionDDay.expected(): AppFunctionDDay =
    AppFunctionDDay(
        id = id,
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats,
        endDate = endDate,
        overflowPolicy = overflowPolicy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        count =
            AppFunctionDDayCount(
                status = count.status,
                remainingDays = count.remainingDays,
                passedDays = count.passedDays,
                years = count.years,
                months = count.months,
            ),
    )
