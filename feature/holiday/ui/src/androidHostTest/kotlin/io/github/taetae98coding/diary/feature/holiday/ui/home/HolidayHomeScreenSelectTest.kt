package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NOT_PROVIDED_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 2026년 2월 6일 금요일 공휴일로 2월 6일부터 8일까지가 연휴이며, 항목은 2월 1일부터 7일까지와 2월 8일부터 14일까지의 두 주를 표시한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScreenSelectTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val targetYear = mutableStateOf<Int?>(null)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-035 카드에서 하루 기간 선택을 완료하면 그 기간으로 MemoAdd 화면 이동이 요청된다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        composeRule.performLongPress(composeRule.dayCenter(day = 6))
        composeRule.performUp()

        navigatedDateRangeList shouldBe listOf(february(day = 6)..february(day = 6))
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-036 주 경계를 넘는 기간 선택을 완료하면 그 기간으로 MemoAdd 화면 이동이 요청된다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        composeRule.performLongPress(composeRule.dayCenter(day = 6))
        composeRule.performMoveTo(composeRule.dayCenter(day = 9))
        composeRule.performUp()

        navigatedDateRangeList shouldBe listOf(february(day = 6)..february(day = 9))
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-054 기간을 선택하는 도중 시스템이 선택을 중단하면 그 기간으로 MemoAdd 화면 이동이 요청된다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        composeRule.performLongPress(composeRule.dayCenter(day = 6))
        composeRule.performMoveTo(composeRule.dayCenter(day = 9))
        composeRule.onRoot().performTouchInput { cancel() }
        composeRule.waitForIdle()

        navigatedDateRangeList shouldBe listOf(february(day = 6)..february(day = 9))
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-055 기간을 선택하는 도중 화면이 재생성되면 MemoAdd 화면 이동이 요청되지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(
            navigateToMemoAdd = { navigatedDateRangeList += it },
            restorationTester = restorationTester,
        )

        composeRule.performLongPress(composeRule.dayCenter(day = 6))
        composeRule.performMoveTo(composeRule.dayCenter(day = 9))
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        composeRule.performUp()

        navigatedDateRangeList shouldBe emptyList()
        composeRule.onNodeWithText(YEAR.toString()).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-037 연휴 기간에 속하지 않는 날짜도 선택해 메모 추가를 시작할 수 있다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        composeRule.performLongPress(composeRule.dayCenter(day = 3))
        composeRule.performUp()

        navigatedDateRangeList shouldBe listOf(february(day = 3)..february(day = 3))
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-038 항목이 표시하는 주 밖으로 드래그해도 선택은 그 주 범위를 넘지 않는다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })
        val start = composeRule.dayCenter(day = 6)

        composeRule.performLongPress(start)
        composeRule.performMoveTo(Offset(x = start.x, y = composeRule.rootBottom()))
        composeRule.performUp()

        navigatedDateRangeList shouldBe listOf(february(day = 6)..february(day = 13))
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-039 오류 안내가 표시된 년도에서는 기간 선택이 시작되지 않는다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(
            fetchHolidayUseCase = previousYearFailFetchHolidayUseCase(),
            navigateToMemoAdd = { navigatedDateRangeList += it },
        )
        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertExists()

        composeRule.performLongPress(composeRule.rootCenter())
        composeRule.performUp()

        navigatedDateRangeList shouldBe emptyList()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-039 제공 없음 안내가 표시된 년도에서는 기간 선택이 시작되지 않는다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setHolidayHomeScreen(
            fetchHolidayUseCase = thisYearNotProvidedFetchHolidayUseCase(),
            navigateToMemoAdd = { navigatedDateRangeList += it },
        )
        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertExists()

        composeRule.performLongPress(composeRule.rootCenter())
        composeRule.performUp()

        navigatedDateRangeList shouldBe emptyList()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-040 기간 선택이 진행되는 동안 년도가 이동하지 않는다`() {
        setHolidayHomeScreen()
        val start = composeRule.dayCenter(day = 6)

        composeRule.performLongPress(start)
        composeRule.performMoveTo(Offset(x = 0F, y = start.y))
        composeRule.performMoveTo(Offset(x = composeRule.rootRight(), y = start.y))

        composeRule.onNodeWithText(YEAR.toString()).assertExists()

        composeRule.performUp()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-053 기간 선택이 진행되는 동안 목록이 세로로 이동하지 않는다`() {
        setHolidayHomeScreen(getGoldenHolidayUseCase = manyGoldenHolidayUseCase())
        composeRule.onAllNodesWithText(manyHolidayName(index = MANY_GROUP_COUNT - 1)).fetchSemanticsNodes().isEmpty() shouldBe true
        val start = composeRule.firstDayCenter(day = MANY_HOLIDAY_DAY)

        composeRule.performLongPress(start)
        composeRule.performMoveTo(Offset(x = start.x, y = 1F))

        composeRule.firstDayCenter(day = MANY_HOLIDAY_DAY) shouldBe start

        composeRule.performUp()
    }

    private fun setHolidayHomeScreen(
        fetchHolidayUseCase: FetchHolidayUseCase = successfulFetchHolidayUseCase(),
        getGoldenHolidayUseCase: GetGoldenHolidayUseCase = selectableGetGoldenHolidayUseCase(),
        navigateToMemoAdd: (LocalDateRange) -> Unit = {},
        restorationTester: StateRestorationTester? = null,
    ) {
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            getGoldenHolidayUseCase = getGoldenHolidayUseCase,
            navigateToMemoAdd = navigateToMemoAdd,
            restorationTester = restorationTester,
        )
    }

    private fun selectableGetGoldenHolidayUseCase(): GetGoldenHolidayUseCase =
        mockk<GetGoldenHolidayUseCase>().also { useCase ->
            every { useCase(parameter = any()) } answers {
                val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
                val groupList =
                    if (parameter.year == YEAR) {
                        listOf(selectableGoldenHolidayGroup())
                    } else {
                        emptyList()
                    }

                flowOf(Result.success(groupList))
            }
        }

    private fun selectableGoldenHolidayGroup() =
        goldenHolidayGroup(
            optionList =
                listOf(
                    goldenHoliday(
                        holidayList = listOf(holiday(name = HOLIDAY_NAME, start = february(day = 6))),
                        start = february(day = 6),
                        endInclusive = february(day = 8),
                    ),
                ),
        )

    // 연휴 항목마다 다른 달의 같은 날짜를 공휴일로 두어, 목록이 한 화면을 넘도록 만든다.
    private fun manyGoldenHolidayUseCase(): GetGoldenHolidayUseCase =
        mockk<GetGoldenHolidayUseCase>().also { useCase ->
            every { useCase(parameter = any()) } answers {
                val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
                val groupList =
                    if (parameter.year == YEAR) {
                        List(MANY_GROUP_COUNT) { index ->
                            val date = LocalDate(year = YEAR, month = index + 1, day = MANY_HOLIDAY_DAY)
                            goldenHolidayGroup(
                                optionList =
                                    listOf(
                                        goldenHoliday(
                                            holidayList = listOf(holiday(name = manyHolidayName(index = index), start = date)),
                                            start = date,
                                            endInclusive = date,
                                        ),
                                    ),
                            )
                        }
                    } else {
                        emptyList()
                    }

                flowOf(Result.success(groupList))
            }
        }

    private fun manyHolidayName(index: Int): String = "$HOLIDAY_NAME$index"

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.firstDayCenter(day: Int): Offset =
        onAllNodesWithText(day.toString())[0]
            .fetchSemanticsNode()
            .boundsInRoot
            .center

    private companion object {
        private const val HOLIDAY_NAME = "공휴일"
        private const val MANY_GROUP_COUNT = 12
        private const val MANY_HOLIDAY_DAY = 6
    }
}
