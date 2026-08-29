package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DDayHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DDAY-HOME-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setDDayHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-DDAY-HOME-FEATURE-001 기본 환경에서 상단 바에 제목을 표시한다`() {
        setDDayHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `TC-DDAY-HOME-FEATURE-002 제목과 뒤로가기 외에 선택할 수 있는 동작을 두지 않는다`() {
        setDDayHomeScaffold()

        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setDDayHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        var clickNavigateUpCount = 0
        setDDayHomeScaffold(
            onEvent = { event ->
                when (event) {
                    is DDayHomeScaffoldEvent.ClickNavigateUp -> clickNavigateUpCount += 1
                }
            },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        clickNavigateUpCount shouldBe 1
    }

    private fun setDDayHomeScaffold(onEvent: (DDayHomeScaffoldEvent) -> Unit = {}) {
        composeRule.setContent {
            DiaryTheme {
                DDayHomeScaffold(onEvent = onEvent)
            }
        }
    }

    public companion object {
        private const val KOREAN_TITLE = "디데이"
        private const val DEFAULT_TITLE = "D-Day"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
