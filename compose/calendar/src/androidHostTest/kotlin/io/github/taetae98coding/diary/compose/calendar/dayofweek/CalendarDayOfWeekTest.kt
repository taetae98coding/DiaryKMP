package io.github.taetae98coding.diary.compose.calendar.dayofweek

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarDayOfWeekTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(sdk = [36], qualifiers = "ko")
    fun `TC-CALENDAR-FEATURE-007 여섯 주 위에 일곱 요일이 한국어 표기로 순서대로 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                Calendar {}
            }
        }

        dayOfWeekTexts(KOREAN_DAY_OF_WEEK_TITLES) shouldBe KOREAN_DAY_OF_WEEK_TITLES
    }

    @Test
    fun `TC-CALENDAR-FEATURE-007 여섯 주 위에 일곱 요일이 영어 표기로 순서대로 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                Calendar {}
            }
        }

        dayOfWeekTexts(DEFAULT_DAY_OF_WEEK_TITLES) shouldBe DEFAULT_DAY_OF_WEEK_TITLES
    }

    @Test
    fun `TC-CALENDAR-FEATURE-008 달을 이동해도 요일 표시는 변하지 않는다`() {
        composeRule.setContent {
            DiaryTheme {
                Calendar {}
            }
        }

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        dayOfWeekTexts(DEFAULT_DAY_OF_WEEK_TITLES) shouldBe DEFAULT_DAY_OF_WEEK_TITLES
    }

    private fun dayOfWeekTexts(titles: List<String>): List<String> =
        composeRule
            .onAllNodes(hasText(text = "", substring = true))
            .fetchSemanticsNodes()
            .map { node ->
                node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
            }.filter { text -> text in titles }

    companion object {
        private val DEFAULT_DAY_OF_WEEK_TITLES = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        private val KOREAN_DAY_OF_WEEK_TITLES = listOf("일", "월", "화", "수", "목", "금", "토")
    }
}
