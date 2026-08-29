package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ROUTINE-HOME-FEATURE-001 표시할 루틴이 없으면 빈 상태 안내를 표시한다`() {
        setRoutineHomeScaffold()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ROUTINE-HOME-FEATURE-001 한국어 환경에서 빈 상태 안내는 아직 루틴이 없습니다이다`() {
        setRoutineHomeScaffold()

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-ROUTINE-HOME-FEATURE-004 빈 상태에서도 루틴 추가를 실행할 수 있다`() {
        val eventList = mutableListOf<RoutineHomeScaffoldEvent>()
        setRoutineHomeScaffold(onEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(RoutineHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `TC-ROUTINE-HOME-FEATURE-004 빈 상태에서도 목록을 당겨 새로고침할 수 있다`() {
        val eventList = mutableListOf<RoutineHomeScaffoldEvent>()
        setRoutineHomeScaffold(onEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineHomeScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-ROUTINE-HOME-FEATURE-005 새로고침이 진행되는 동안에도 빈 상태 안내를 유지한다`() {
        setRoutineHomeScaffold(uiStateProvider = { RoutineHomeUiState(isRefreshing = true) })

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-ROUTINE-LIST-DETAIL-FEATURE-003 상세 영역에 루틴 추가가 놓이면 루틴 추가 버튼이 표시되지 않는다`() {
        setRoutineHomeScaffold(componentVisibleProvider = { RoutineHomeScaffoldComponentVisible(isAddButtonVisible = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ROUTINE-LIST-DETAIL-FEATURE-004 추가 버튼 표시 상태이면 루틴 추가 버튼이 표시된다`() {
        setRoutineHomeScaffold(componentVisibleProvider = { RoutineHomeScaffoldComponentVisible(isAddButtonVisible = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setRoutineHomeScaffold(
        onEvent: (RoutineHomeScaffoldEvent) -> Unit = {},
        uiStateProvider: () -> RoutineHomeUiState = { RoutineHomeUiState() },
        componentVisibleProvider: () -> RoutineHomeScaffoldComponentVisible = { RoutineHomeScaffoldComponentVisible() },
    ) {
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScaffold(
                    onEvent = onEvent,
                    uiStateProvider = uiStateProvider,
                    componentVisibleProvider = componentVisibleProvider,
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_EMPTY_TITLE = "No routines yet"
        private const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a routine."
        private const val KOREAN_EMPTY_TITLE = "아직 루틴이 없습니다"
        private const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 루틴을 만들 수 있습니다"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add routine"
    }
}
