package io.github.taetae98coding.diary.work.appaction.appfunction.lunar

import android.content.Context
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.github.taetae98coding.diary.work.appaction.appfunction.core.returnValueListAs
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.lunar.AppActionLunarDate
import io.github.taetae98coding.diary.work.appaction.lunar.LunarAppAction
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
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
class LunarAppFunctionServiceTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    private val lunarAppAction = mockk<LunarAppAction>()

    private lateinit var tester: AppFunctionServiceTester

    @Before
    fun setUp() {
        startKoin { modules(module { single { lunarAppAction } }) }
        tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `음력 동작을 제공한다`() =
        runBlocking<Unit> {
            tester.functionNameList().filter { name -> name.startsWith("lunar") } shouldContainExactlyInAnyOrder listOf(LUNAR_LIST)
        }

    @Test
    fun `음력 날짜는 날짜 범위를 공통 동작에 넘기고 항목 목록을 같은 값으로 돌려준다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val lunarDate =
                AppActionLunarDate(
                    solarDate = rangeStart,
                    lunarYear = Arb.int(1900..2100).next(),
                    lunarMonth = Arb.int(1..12).next(),
                    lunarDay = Arb.int(1..30).next(),
                    isLeapMonth = Arb.boolean().next(),
                )
            coEvery { lunarAppAction.list(startDate = any(), endDate = any()) } returns listOf(lunarDate)

            val response =
                tester.execute(name = LUNAR_LIST) {
                    setString(START_DATE, rangeStart)
                    setString(END_DATE, rangeEnd)
                }

            response.returnValueListAs<AppFunctionLunarDate>() shouldBe
                listOf(
                    AppFunctionLunarDate(
                        solarDate = lunarDate.solarDate,
                        lunarYear = lunarDate.lunarYear,
                        lunarMonth = lunarDate.lunarMonth,
                        lunarDay = lunarDate.lunarDay,
                        isLeapMonth = lunarDate.isLeapMonth,
                    ),
                )
            coVerify(exactly = 1) { lunarAppAction.list(startDate = rangeStart, endDate = rangeEnd) }
        }

    @Test
    fun `앱 기능 실패는 앱 실패로 같은 메시지를 알린다`() =
        runBlocking<Unit> {
            val dateRange = Arb.localDateRange().next()
            val rangeStart = dateRange.start.toString()
            val rangeEnd = dateRange.endInclusive.toString()
            val message = Arb.nonBlankString().next()
            coEvery { lunarAppAction.list(startDate = any(), endDate = any()) } throws AppActionException.Failure(message = message)

            val response =
                tester.execute(name = LUNAR_LIST) {
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
private const val LUNAR_LIST = "lunarList"
