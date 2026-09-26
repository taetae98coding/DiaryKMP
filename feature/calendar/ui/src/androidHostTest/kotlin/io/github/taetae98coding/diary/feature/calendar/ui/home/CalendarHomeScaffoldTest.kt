package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayOfWeek
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-001 캘린더 홈 화면에 오늘이 속한 달의 캘린더가 표시된다`() {
        val sundayOfFirstWeek =
            Clock.System
                .todayIn(TimeZone.currentSystemDefault())
                .yearMonth.firstDay
                .sundayOfWeek()

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {})
            }
        }

        numberTexts() shouldBe List(DAYS_PER_CALENDAR) { sundayOfFirstWeek.plus(it, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    @Config(sdk = [36], qualifiers = "ko")
    fun `TC-CALENDAR-HOME-FEATURE-002 상단 바 중앙에 오늘이 속한 달의 년도와 월이 한국어 형식으로 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {})
            }
        }

        composeRule.onNodeWithText(koreanTitle(today.yearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-002 상단 바 중앙에 오늘이 속한 달의 년도와 월이 영어 형식으로 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {})
            }
        }

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-003 달을 이동하면 상단 바 제목이 이동한 달로 바뀐다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {})
            }
        }

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-087 화면이 재생성되어도 상단 바 제목이 보던 달을 유지한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {})
            }
        }
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-101 시스템이 앱을 정리했다가 다시 만들어도 상단 바 제목이 보던 달을 유지한다`() {
        val nextYearMonth = nextYearMonth()
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {})
            }
        }
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        // 시스템이 정리한 앱을 다시 만들 때도 화면 재생성과 같이 저장해 둔 화면 상태에서 복원한다.
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-088 다른 앱에 다녀와도 상단 바 제목이 보던 달을 유지한다`() {
        val nextYearMonth = nextYearMonth()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    CalendarHomeScaffold(onEvent = {})
                }
            }
        }
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-089 앱을 다시 실행하면 오늘이 속한 달부터 다시 본다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        var launchCount by mutableIntStateOf(0)

        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                DiaryTheme {
                    CalendarHomeScaffold(onEvent = {})
                }
            }
        }
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(englishTitle(nextYearMonth())).assertIsDisplayed()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    private fun nextYearMonth(): YearMonth =
        Clock.System
            .todayIn(TimeZone.currentSystemDefault())
            .yearMonth.firstDay
            .plus(1, DateTimeUnit.MONTH)
            .yearMonth

    private fun koreanTitle(yearMonth: YearMonth): String = CalendarHomeTestFixture.koreanTitle(yearMonth)

    private fun englishTitle(yearMonth: YearMonth): String = CalendarHomeTestFixture.englishTitle(yearMonth)

    private fun numberTexts(): List<String> =
        composeRule
            .onAllNodes(hasText(text = "", substring = true))
            .fetchSemanticsNodes()
            .filterNot { node ->
                node.config
                    .getOrNull(SemanticsProperties.ContentDescription)
                    ?.contains(CalendarHomeTestFixture.TODAY_BUTTON_DESCRIPTION) == true
            }.map { node ->
                node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
            }.filter { text -> text.isNotEmpty() && text.all(Char::isDigit) }

    companion object {
        private const val DAYS_PER_CALENDAR = 42
    }
}
