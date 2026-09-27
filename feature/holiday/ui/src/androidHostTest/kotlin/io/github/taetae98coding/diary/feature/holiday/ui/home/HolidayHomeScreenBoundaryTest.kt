package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ANNUAL_LEAVE_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NEXT_OPTION_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NOT_PROVIDED_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScreenBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val targetYear = mutableStateOf<Int?>(null)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-045 연차 개수를 바꾸면 각 항목은 첫 번째 대안으로 돌아간다`() {
        val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
        every { getGoldenHolidayUseCase(parameter = any()) } answers {
            val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
            val groupList =
                when {
                    parameter.year != YEAR -> emptyList()
                    parameter.annualLeaveCount == 0 -> listOf(twoOptionGroup(year = YEAR, firstStartDay = 7))
                    else -> listOf(twoOptionGroup(year = YEAR, firstStartDay = 6))
                }

            flowOf(Result.success(groupList))
        }
        composeRule.setHolidayHomeScreen(targetYear = targetYear, getGoldenHolidayUseCase = getGoldenHolidayUseCase)

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick()

        composeRule.onNodeWithText(FIRST_OPTION_POSITION).assertExists()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertDoesNotExist()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-046 저장된 공휴일이 바뀌어 항목 내용이 달라지면 그 항목은 첫 번째 대안으로 돌아간다`() {
        val groupListFlow = MutableSharedFlow<Result<List<GoldenHolidayGroup>>>(replay = 1)
        groupListFlow.tryEmit(Result.success(listOf(twoOptionGroup(year = YEAR, firstStartDay = 7))))
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            getGoldenHolidayUseCase = thisYearFlowGetGoldenHolidayUseCase(groupListFlow = groupListFlow),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()

        composeRule.runOnIdle {
            groupListFlow.tryEmit(Result.success(listOf(twoOptionGroup(year = YEAR, firstStartDay = 7, holidayName = RENAMED_HOLIDAY_NAME))))
        }

        composeRule.onNodeWithText(FIRST_OPTION_POSITION).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-047 다시 받아온 결과로 항목 내용이 그대로이면 고른 대안이 유지된다`() {
        val groupListFlow = MutableSharedFlow<Result<List<GoldenHolidayGroup>>>(replay = 1)
        groupListFlow.tryEmit(Result.success(listOf(twoOptionGroup(year = YEAR, firstStartDay = 7))))
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            getGoldenHolidayUseCase = thisYearFlowGetGoldenHolidayUseCase(groupListFlow = groupListFlow),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()

        composeRule.runOnIdle {
            groupListFlow.tryEmit(Result.success(listOf(twoOptionGroup(year = YEAR, firstStartDay = 7))))
        }

        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-049 오류 안내가 표시된 년도는 다른 화면이나 다른 앱에서 돌아오면 다시 동기화한다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { fetchHolidayUseCase(parameter = YEAR - 1) } returnsMany
            listOf(
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                Result.success(providedHolidayList()),
            )

        assertListShownAfterReturn(fetchHolidayUseCase = fetchHolidayUseCase, description = DEFAULT_ERROR_DESCRIPTION)
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-049 제공 없음 안내가 표시된 년도는 다른 화면이나 다른 앱에서 돌아오면 다시 동기화한다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { fetchHolidayUseCase(parameter = YEAR) } returnsMany
            listOf(
                Result.success(emptyList()),
                Result.success(providedHolidayList()),
            )

        assertListShownAfterReturn(fetchHolidayUseCase = fetchHolidayUseCase, description = DEFAULT_NOT_PROVIDED_DESCRIPTION)
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-050 화면이 재생성되어도 보던 년도와 연차 개수가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            val state = rememberHolidayHomeScaffoldState(initialYear = YEAR)
            val year = targetYear.value

            LaunchedEffect(year) {
                year?.let { state.animateScrollTo(year = it) }
            }
            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = state,
                    yearContent = { _, _ ->
                        GoldenHolidayYear(
                            uiStateProvider = { HolidayHomeYearUiState.Loaded() },
                            onEvent = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            }
        }

        repeat(2) { composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick() }
        scrollTo(year = YEAR + 1)
        composeRule.onNodeWithText("2027").assertExists()
        // 복원 뒤 다시 이동하지 않도록 이동 요청을 비운다.
        composeRule.runOnIdle { targetYear.value = null }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText("2027").assertExists()
        composeRule.onNode(annualLeaveCount(count = 2)).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-051 화면이 재생성되어도 고른 대안이 유지된다`() {
        val uiState = HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(twoOptionGroup(year = YEAR, firstStartDay = 7)))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                GoldenHolidayYear(
                    uiStateProvider = { uiState },
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-052 다른 앱에 다녀와도 보던 년도와 연차 개수와 고른 대안이 유지된다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
        every { getGoldenHolidayUseCase(parameter = any()) } answers {
            val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
            val groupList = if (parameter.year == YEAR + 1) listOf(twoOptionGroup(year = YEAR + 1, firstStartDay = 6)) else emptyList()

            flowOf(Result.success(groupList))
        }
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            getGoldenHolidayUseCase = getGoldenHolidayUseCase,
            lifecycleOwner = lifecycleOwner,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick()
        scrollTo(year = YEAR + 1)
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()

        leaveAndReturn(lifecycleOwner = lifecycleOwner)

        composeRule.onNodeWithText("2027").assertExists()
        composeRule.onNode(annualLeaveCount(count = 1)).assertExists()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-DATA-014 년도가 드러난 채 다른 화면이나 다른 앱에서 돌아오면 동기화를 다시 요청한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val fetchHolidayUseCase = successfulFetchHolidayUseCase()
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            lifecycleOwner = lifecycleOwner,
        )

        leaveAndReturn(lifecycleOwner = lifecycleOwner)

        coVerify(exactly = 2) { fetchHolidayUseCase(parameter = YEAR - 1) }
        coVerify(exactly = 2) { fetchHolidayUseCase(parameter = YEAR) }
        coVerify(exactly = 2) { fetchHolidayUseCase(parameter = YEAR + 1) }
    }

    private fun assertListShownAfterReturn(
        fetchHolidayUseCase: FetchHolidayUseCase,
        description: String,
    ) {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
            lifecycleOwner = lifecycleOwner,
        )

        composeRule.onNodeWithText(description).assertExists()

        leaveAndReturn(lifecycleOwner = lifecycleOwner)

        composeRule.onNodeWithText(description).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
    }

    // 다른 화면이나 다른 앱으로 가면 화면이 멈췄다가, 돌아오면 다시 시작된다.
    private fun leaveAndReturn(lifecycleOwner: TestLifecycleOwner) {
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.waitForIdle()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()
    }

    // 연차 개수는 연차 라벨과 같은 줄에 있는 숫자다. 주별 날짜 숫자와 구분한다.
    private fun annualLeaveCount(count: Int): SemanticsMatcher = hasText(count.toString()) and hasAnySibling(hasText(DEFAULT_ANNUAL_LEAVE_LABEL))

    private fun scrollTo(year: Int) {
        composeRule.runOnIdle { targetYear.value = year }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val HOLIDAY_NAME = "설날"
        private const val RENAMED_HOLIDAY_NAME = "구정"
        private const val FIRST_OPTION_POSITION = "1 / 2"
        private const val SECOND_OPTION_POSITION = "2 / 2"

        private fun thisYearFlowGetGoldenHolidayUseCase(groupListFlow: MutableSharedFlow<Result<List<GoldenHolidayGroup>>>): GetGoldenHolidayUseCase =
            mockk<GetGoldenHolidayUseCase>().also { useCase ->
                every { useCase(parameter = any()) } answers {
                    if (firstArg<GetGoldenHolidayUseCase.Parameter>().year == YEAR) groupListFlow else flowOf(Result.success(emptyList()))
                }
            }

        // 2월 16일부터 18일까지의 공휴일에 연차를 앞에 붙인 안과 뒤에 붙인 안을 담는다.
        private fun twoOptionGroup(
            year: Int,
            firstStartDay: Int,
            holidayName: String = HOLIDAY_NAME,
        ): GoldenHolidayGroup {
            val holidayList = listOf(yearHoliday(year = year, name = holidayName))

            return goldenHolidayGroup(
                optionList =
                    listOf(
                        goldenHoliday(
                            holidayList = holidayList,
                            start = february(year = year, day = firstStartDay),
                            endInclusive = february(year = year, day = 18),
                        ),
                        goldenHoliday(
                            holidayList = holidayList,
                            start = february(year = year, day = 16),
                            endInclusive = february(year = year, day = 16 + 18 - firstStartDay),
                        ),
                    ),
            )
        }

        private fun yearHoliday(
            year: Int,
            name: String,
        ): Holiday =
            holiday(
                name = name,
                start = february(year = year, day = 16),
                endInclusive = february(year = year, day = 18),
            )

        private fun february(
            year: Int,
            day: Int,
        ): LocalDate = LocalDate(year = year, month = Month.FEBRUARY, day = day)
    }
}
