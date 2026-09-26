package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenWeatherScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-073 아이템을 모두 볼 수 없는 주에서도 처음 도착한 날씨가 표시된다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        setCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            memoList = memoList(),
        )
        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()

        weatherReportFlow.value = weatherReport(temperature = FIRST_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-074 사용자가 아이템을 스크롤해 둔 주에서도 처음 도착한 날씨가 표시된다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        val memoList = memoList()
        setCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            memoList = memoList,
        )
        scrollMemoWeekToLastMemo(memoList = memoList)
        composeRule.onNodeWithText(memoTitle(index = 0)).isNotDisplayed() shouldBe true

        weatherReportFlow.value = weatherReport(temperature = FIRST_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DOMAIN-009 날씨를 처음 표시한 뒤에는 날씨가 갱신되어도 아이템 스크롤을 되돌리지 않는다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        val memoList = memoList()
        setCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            memoList = memoList,
        )
        weatherReportFlow.value = weatherReport(temperature = FIRST_TEMPERATURE)
        composeRule.waitForIdle()
        scrollMemoWeekToLastMemo(memoList = memoList)
        composeRule.onNodeWithText(FIRST_TEMPERATURE_TEXT).isNotDisplayed() shouldBe true

        weatherReportFlow.value = weatherReport(temperature = UPDATED_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATED_TEMPERATURE_TEXT).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(memoTitle(index = MEMO_COUNT - 1)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DOMAIN-018 날씨가 사라졌다가 다시 채워져도 아이템 스크롤을 되돌리지 않는다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        val memoList = memoList()
        setCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            memoList = memoList,
        )
        weatherReportFlow.value = weatherReport(temperature = FIRST_TEMPERATURE)
        composeRule.waitForIdle()
        weatherReportFlow.value = CalendarWeatherReport()
        composeRule.waitForIdle()
        scrollMemoWeekToLastMemo(memoList = memoList)
        composeRule.onNodeWithText(memoTitle(index = 0)).isNotDisplayed() shouldBe true

        weatherReportFlow.value = weatherReport(temperature = UPDATED_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATED_TEMPERATURE_TEXT).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(memoTitle(index = MEMO_COUNT - 1)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DOMAIN-010 화면이 재생성되어도 이미 표시 중인 날씨로는 아이템 스크롤을 되돌리지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val memoList = memoList()
        restorationTester.setContent(
            calendarHomeScreen(
                weatherReportFlow = MutableStateFlow(weatherReport(temperature = FIRST_TEMPERATURE)),
                memoList = memoList,
            ),
        )
        scrollMemoWeekToLastMemo(memoList = memoList)
        composeRule.onNodeWithText(FIRST_TEMPERATURE_TEXT).isNotDisplayed() shouldBe true

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TEMPERATURE_TEXT).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(memoTitle(index = MEMO_COUNT - 1)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DOMAIN-011 화면이 재생성된 뒤 처음 도착한 날씨는 아이템 스크롤을 되돌린다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        val memoList = memoList()
        restorationTester.setContent(
            calendarHomeScreen(
                weatherReportFlow = weatherReportFlow,
                memoList = memoList,
            ),
        )
        scrollMemoWeekToLastMemo(memoList = memoList)
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        weatherReportFlow.value = weatherReport(temperature = FIRST_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DOMAIN-020 다른 화면에서 돌아온 뒤 처음 도착한 날씨는 아이템 스크롤을 되돌린다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        val memoList = memoList()
        val isShown = mutableStateOf(true)
        setReturnableCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            memoList = memoList,
            isShown = isShown,
            lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED),
        )
        showWeatherThenClear(weatherReportFlow = weatherReportFlow)

        composeRule.runOnIdle { isShown.value = false }
        composeRule.runOnIdle { isShown.value = true }
        composeRule.waitForIdle()
        scrollMemoWeekToLastMemo(memoList = memoList)
        composeRule.onNodeWithText(memoTitle(index = 0)).isNotDisplayed() shouldBe true

        weatherReportFlow.value = weatherReport(temperature = UPDATED_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATED_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DOMAIN-021 다른 앱에 다녀온 뒤 도착한 날씨는 아이템 스크롤을 되돌리지 않는다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        val memoList = memoList()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setReturnableCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            memoList = memoList,
            isShown = mutableStateOf(true),
            lifecycleOwner = lifecycleOwner,
        )
        showWeatherThenClear(weatherReportFlow = weatherReportFlow)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()
        scrollMemoWeekToLastMemo(memoList = memoList)
        composeRule.onNodeWithText(memoTitle(index = 0)).isNotDisplayed() shouldBe true

        weatherReportFlow.value = weatherReport(temperature = UPDATED_TEMPERATURE)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATED_TEMPERATURE_TEXT).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(memoTitle(index = MEMO_COUNT - 1)).assertIsDisplayed()
    }

    private fun showWeatherThenClear(weatherReportFlow: MutableStateFlow<CalendarWeatherReport>) {
        weatherReportFlow.value = weatherReport(temperature = FIRST_TEMPERATURE)
        composeRule.waitForIdle()
        weatherReportFlow.value = CalendarWeatherReport()
        composeRule.waitForIdle()
    }

    /** 다른 화면으로 이동하면 캘린더 홈 화면이 구성에서 빠지고, 돌아오면 보던 달과 스크롤 상태 그대로 다시 구성된다. */
    private fun setReturnableCalendarHomeScreen(
        weatherReportFlow: MutableStateFlow<CalendarWeatherReport>,
        memoList: List<CalendarMemo>,
        isShown: MutableState<Boolean>,
        lifecycleOwner: TestLifecycleOwner,
    ) {
        val memoViewModel = memoViewModel(memoList = memoList)
        val weatherViewModel = weatherViewModel(weatherReportFlow = weatherReportFlow)
        val holidayViewModel = holidayViewModel()
        val birthdayViewModel = birthdayViewModel()
        val syncViewModel = syncViewModel()

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    if (isShown.value) {
                        CalendarHomeScreen(
                            navigateToMemoDetail = {},
                            navigateToMemoAdd = {},
                            navigateToContactDetail = {},
                            birthdayViewModel = birthdayViewModel,
                            navigateToFilter = {},
                            state = state,
                            holidayViewModel = holidayViewModel,
                            memoViewModel = memoViewModel,
                            weatherViewModel = weatherViewModel,
                            syncViewModel = syncViewModel,
                            permissionManager = rememberPermissionManager(),
                        )
                    }
                }
            }
        }
    }

    private fun memoViewModel(memoList: List<CalendarMemo>): CalendarHomeMemoViewModel =
        mockk<CalendarHomeMemoViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.memoList } returns MutableStateFlow(memoList)
            every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
        }

    private fun scrollMemoWeekToLastMemo(memoList: List<CalendarMemo>) {
        composeRule
            .onNode(isVerticallyScrollable() and hasAnyDescendant(hasText(text = memoTitle(index = 0))))
            .performScrollToKey(memoList.last().id)
    }

    private fun isVerticallyScrollable(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

    private fun weatherReport(temperature: Double): CalendarWeatherReport =
        CalendarWeatherReport(
            weatherList =
                listOf(
                    calendarWeather(
                        date = july(day = WEATHER_DAY),
                        temperature = CalendarWeatherTemperature.Current(value = temperature),
                        descriptionList = listOf(SUNNY_DESCRIPTION),
                    ),
                ),
        )

    private fun setCalendarHomeScreen(
        weatherReportFlow: MutableStateFlow<CalendarWeatherReport>,
        memoList: List<CalendarMemo>,
    ) {
        composeRule.setContent(
            calendarHomeScreen(
                weatherReportFlow = weatherReportFlow,
                memoList = memoList,
            ),
        )
    }

    private fun calendarHomeScreen(
        weatherReportFlow: MutableStateFlow<CalendarWeatherReport>,
        memoList: List<CalendarMemo>,
    ): @Composable () -> Unit {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(memoList)
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        return {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    state = state,
                    holidayViewModel = holidayViewModel(),
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(weatherReportFlow = weatherReportFlow),
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private fun memoList(): List<CalendarMemo> = List(MEMO_COUNT) { index -> memo(title = memoTitle(index = index)) }

    private fun memoTitle(index: Int): String = "$MEMO_TITLE_PREFIX$index"

    private fun memo(title: String): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = fixtureMonkey.giveMeOne(),
            dateTime = MemoDateTime.AllDay(dateRange = july(day = MEMO_START_DAY)..july(day = WEATHER_DAY)),
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val MEMO_COUNT = 6
        private const val MEMO_TITLE_PREFIX = "메모"
        private const val MEMO_START_DAY = 12
        private const val WEATHER_DAY = 15
        private const val SUNNY_DESCRIPTION = "맑음"
        private const val FIRST_TEMPERATURE = 24.3
        private const val UPDATED_TEMPERATURE = 26.7
        private const val FIRST_TEMPERATURE_TEXT = "24.3°"
        private const val UPDATED_TEMPERATURE_TEXT = "26.7°"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
